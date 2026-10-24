package backend;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FileIntegrityCheckerTest {
    @Test
    void hashesKnownContent() throws Exception {
        FileIntegrityChecker checker = new FileIntegrityChecker();
        Path file = Files.createTempFile("taskx", ".txt");
        Files.write(file, "hello".getBytes());

        assertEquals("5d41402abc4b2a76b9719d911017c592", checker.md5(file.toFile()));
        assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824", checker.sha256(file.toFile()));

        Files.deleteIfExists(file);
    }

    @Test
    void detectsModifiedNewAndDeletedFiles() throws Exception {
        FileIntegrityChecker checker = new FileIntegrityChecker();
        Path dir = Files.createTempDirectory("taskx-fim");
        Path a = Files.write(dir.resolve("a.txt"), "one".getBytes());
        Path b = Files.write(dir.resolve("b.txt"), "two".getBytes());

        Map<String, String> baseline = checker.snapshot(dir.toFile(), "SHA-256");

        Files.write(a, "changed".getBytes());
        Files.delete(b);
        Files.write(dir.resolve("c.txt"), "three".getBytes());

        List<FileIntegrityChecker.VerificationResult> results =
            checker.verify(baseline, dir.toFile(), "SHA-256");

        assertEquals(FileIntegrityChecker.Status.MODIFIED, find(results, "a.txt").getStatus());
        assertEquals(FileIntegrityChecker.Status.DELETED, find(results, "b.txt").getStatus());
        assertEquals(FileIntegrityChecker.Status.NEW, find(results, "c.txt").getStatus());
    }

    private FileIntegrityChecker.VerificationResult find(
        List<FileIntegrityChecker.VerificationResult> results, String path
    ) {
        return results.stream()
            .filter(r -> r.getPath().equals(path))
            .findFirst()
            .orElseThrow();
    }
}
