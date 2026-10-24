package backend;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class NetworkScanner {
    public static class PortResult {
        private final int port;
        private final String service;
        private final String risk;
        private final boolean open;
        private final long latencyMs;

        public PortResult(int port, String service, String risk, boolean open, long latencyMs) {
            this.port = port;
            this.service = service;
            this.risk = risk;
            this.open = open;
            this.latencyMs = latencyMs;
        }

        public int getPort() { return port; }
        public String getService() { return service; }
        public String getRisk() { return risk; }
        public boolean isOpen() { return open; }
        public long getLatencyMs() { return latencyMs; }
    }

    private static final class PortInfo {
        final String service;
        final String risk;

        PortInfo(String service, String risk) {
            this.service = service;
            this.risk = risk;
        }
    }

    private static final ConcurrentMap<Integer, PortInfo> KNOWN_PORTS = new ConcurrentHashMap<>();

    static {
        addPort(20, "FTP-DATA", "HIGH");
        addPort(21, "FTP", "HIGH");
        addPort(22, "SSH", "MEDIUM");
        addPort(23, "TELNET", "HIGH");
        addPort(25, "SMTP", "MEDIUM");
        addPort(53, "DNS", "INFO");
        addPort(80, "HTTP", "MEDIUM");
        addPort(110, "POP3", "MEDIUM");
        addPort(139, "NETBIOS", "HIGH");
        addPort(143, "IMAP", "MEDIUM");
        addPort(443, "HTTPS", "LOW");
        addPort(445, "SMB", "HIGH");
        addPort(1433, "MSSQL", "HIGH");
        addPort(1521, "ORACLE", "HIGH");
        addPort(3306, "MYSQL", "HIGH");
        addPort(3389, "RDP", "HIGH");
        addPort(5432, "POSTGRESQL", "HIGH");
        addPort(5900, "VNC", "HIGH");
        addPort(6379, "REDIS", "HIGH");
        addPort(8080, "HTTP-ALT", "MEDIUM");
        addPort(8443, "HTTPS-ALT", "LOW");
        addPort(4444, "METASPLOIT", "HIGH");
    }

    private static void addPort(int port, String service, String risk) {
        KNOWN_PORTS.put(port, new PortInfo(service, risk));
    }

    public static List<Integer> commonPorts() {
        List<Integer> ports = new ArrayList<>(KNOWN_PORTS.keySet());
        Collections.sort(ports);
        return ports;
    }

    public static List<Integer> webPorts() {
        return Arrays.asList(80, 443, 8080, 8443);
    }

    public static List<Integer> databasePorts() {
        return Arrays.asList(1433, 1521, 3306, 5432, 6379);
    }

    public List<PortResult> scan(String host, List<Integer> ports, int timeoutMs, int threads) throws InterruptedException {
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("Target host is required.");
        }
        if (ports == null || ports.isEmpty()) {
            return Collections.emptyList();
        }

        ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, threads));
        List<Future<PortResult>> futures = new ArrayList<>();

        for (Integer port : ports) {
            if (port == null || port < 1 || port > 65535) {
                continue;
            }
            futures.add(executor.submit(() -> scanPort(host.trim(), port, timeoutMs)));
        }

        List<PortResult> results = new ArrayList<>();
        try {
            for (Future<PortResult> future : futures) {
                try {
                    results.add(future.get());
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof RuntimeException) {
                        throw (RuntimeException) cause;
                    }
                }
            }
        } finally {
            executor.shutdownNow();
        }

        results.sort((a, b) -> Integer.compare(a.getPort(), b.getPort()));
        return results;
    }

    public PortResult scanPort(String host, int port, int timeoutMs) {
        long start = System.nanoTime();
        boolean open = false;

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), Math.max(50, timeoutMs));
            open = true;
        } catch (IOException ignored) {
        }

        long latencyMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        PortInfo info = KNOWN_PORTS.get(port);
        String service = info == null ? "UNKNOWN" : info.service;
        String risk = info == null ? "INFO" : info.risk;

        return new PortResult(port, service, risk, open, latencyMs);
    }
}
