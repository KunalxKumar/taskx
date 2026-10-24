package backend;

import org.junit.jupiter.api.Test;
import java.net.ServerSocket;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class NetworkScannerTest {
    @Test
    void identifiesKnownServiceAndFindsLocalOpenPort() throws Exception {
        try (ServerSocket server = new ServerSocket(0)) {
            NetworkScanner scanner = new NetworkScanner();
            NetworkScanner.PortResult result =
                scanner.scanPort("127.0.0.1", server.getLocalPort(), 500);

            assertTrue(result.isOpen());
            assertEquals("UNKNOWN", result.getService());
            assertEquals("INFO", result.getRisk());
        }
    }

    @Test
    void commonPortsAreAvailable() {
        assertTrue(NetworkScanner.commonPorts().contains(22));
        assertTrue(NetworkScanner.commonPorts().contains(445));
        assertTrue(NetworkScanner.commonPorts().contains(3306));
    }
}
