package backend;

import model.Alert;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.List;

public class ResilientAlertStorage implements AlertStorage {
    private final AlertStorage delegate;
    private final boolean usingDatabase;

    public ResilientAlertStorage() {
        this.delegate = createDelegate();
        this.usingDatabase = delegate instanceof JdbcAlertStorage;
    }

    private AlertStorage createDelegate() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            JdbcAlertStorage jdbc = new JdbcAlertStorage();
            if (jdbc.isAvailable()) {
                return jdbc;
            }
        } catch (Throwable ignored) {
        }

        System.err.println("MySQL unavailable. Using local alert storage.");
        return new FileAlertStorage();
    }

    public boolean isUsingDatabase() {
        return usingDatabase;
    }

    @Override
    public void save(Alert alert) {
        delegate.save(alert);
    }

    @Override
    public List<Alert> loadAll() {
        return delegate.loadAll();
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    public void exportToCsv(File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("timestamp,severity,source_ip,type,message");
            for (Alert alert : loadAll()) {
                writer.println(csv(alert.getTimestamp()) + ","
                    + csv(alert.getSeverity()) + ","
                    + csv(alert.getSourceIP()) + ","
                    + csv(alert.getType()) + ","
                    + csv(alert.getMessage()));
            }
        }
    }

    private String csv(String value) {
        if (value == null) {
            return """";
        }
        return """ + value.replace(""", """") + """;
    }
}
