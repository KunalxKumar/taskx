package backend;

import model.Alert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ResilientAlertStorageTest {
    @Test
    void fallsBackWhenMySqlIsUnavailable() {
        String oldUrl = System.getProperty("taskx.db.url");
        System.setProperty("taskx.db.url", "jdbc:mysql://127.0.0.1:1/taskx?connectTimeout=100&socketTimeout=100");

        try {
            ResilientAlertStorage storage = new ResilientAlertStorage();
            assertFalse(storage.isUsingDatabase());

            Alert alert = new Alert("LOW", "127.0.0.1", "TEST", "fallback");
            storage.save(alert);
            assertFalse(storage.loadAll().isEmpty());
        } finally {
            if (oldUrl == null) System.clearProperty("taskx.db.url");
            else System.setProperty("taskx.db.url", oldUrl);
        }
    }
}
