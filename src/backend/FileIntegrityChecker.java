package backend;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileIntegrityChecker {
    public enum Status {
        MATCHED, MODIFIED, DELETED, NEW
    }

    public static class VerificationResult {
        private final String path;
        private final Status status;
        private final String expectedHash;
        private final String actualHash;

        public VerificationResult(String path, Status status, String expectedHash, String actualHash) {
            this.path = path;
            this.status = status;
            this.expectedHash = expectedHash;
            this.actualHash = actualHash;
        }

        public String getPath() { return path; }
        public Status getStatus() { return status; }
        public String getExpectedHash() { return expectedHash; }
        public String getActualHash() { return actualHash; }
    }

    public String hash(File file, String algorithm) throws IOException, NoSuchAlgorithmException {
        if (file == null || !file.isFile()) {
            throw new FileNotFoundException("File does not exist: " + file);
        }

        MessageDigest digest = MessageDigest.getInstance(algorithm);
        byte[] buffer = new byte[8192];

        try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }

        return toHex(digest.digest());
    }

    public String md5(File file) throws IOException, NoSuchAlgorithmException {
        return hash(file, "MD5");
    }

    public String sha1(File file) throws IOException, NoSuchAlgorithmException {
        return hash(file, "SHA-1");
    }

    public String sha256(File file) throws IOException, NoSuchAlgorithmException {
        return hash(file, "SHA-256");
    }

    public Map<String, String> snapshot(File directory, String algorithm) throws IOException, NoSuchAlgorithmException {
        if (directory == null || !directory.isDirectory()) {
            throw new NotDirectoryException(String.valueOf(directory));
        }

        Map<String, String> manifest = new TreeMap<>();
        Path root = directory.toPath().toAbsolutePath().normalize();

        try (Stream<Path> stream = Files.walk(root)) {
            List<Path> files = stream.filter(Files::isRegularFile).collect(Collectors.toList());
            for (Path file : files) {
                String relative = root.relativize(file.toAbsolutePath().normalize()).toString();
                manifest.put(relative, hash(file.toFile(), algorithm));
            }
        }

        return manifest;
    }

    public List<VerificationResult> verify(Map<String, String> baseline, File directory, String algorithm)
        throws IOException, NoSuchAlgorithmException {

        Map<String, String> actual = snapshot(directory, algorithm);
        List<VerificationResult> results = new ArrayList<>();

        for (Map.Entry<String, String> entry : baseline.entrySet()) {
            String current = actual.get(entry.getKey());
            if (current == null) {
                results.add(new VerificationResult(
                    entry.getKey(), Status.DELETED, entry.getValue(), null
                ));
            } else if (entry.getValue().equalsIgnoreCase(current)) {
                results.add(new VerificationResult(
                    entry.getKey(), Status.MATCHED, entry.getValue(), current
                ));
            } else {
                results.add(new VerificationResult(
                    entry.getKey(), Status.MODIFIED, entry.getValue(), current
                ));
            }
        }

        for (Map.Entry<String, String> entry : actual.entrySet()) {
            if (!baseline.containsKey(entry.getKey())) {
                results.add(new VerificationResult(
                    entry.getKey(), Status.NEW, null, entry.getValue()
                ));
            }
        }

        results.sort(Comparator.comparing(VerificationResult::getPath));
        return results;
    }

    public void writeManifest(Map<String, String> manifest, File output) throws IOException {
        File parent = output.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output)))) {
            for (Map.Entry<String, String> entry : manifest.entrySet()) {
                writer.println(entry.getValue() + "  " + entry.getKey());
            }
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(Character.forDigit((value >>> 4) & 0x0f, 16));
            builder.append(Character.forDigit(value & 0x0f, 16));
        }
        return builder.toString();
    }
}
