package backend;

import model.Alert;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileAlertStorage implements AlertStorage {
    private static final String FILE_PATH = "data/alerts.log";

    public FileAlertStorage() {
        // Ensure data directory exists
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }

    @Override
    public synchronized void save(Alert alert) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(FILE_PATH, true)))) {
            out.println(alert.toLogLine());
        } catch (IOException e) {
            System.err.println("Error saving alert: " + e.getMessage());
        }
    }

    @Override
    public List<Alert> loadAll() {
        List<Alert> alerts = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return alerts;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Alert alert = Alert.fromLogLine(line);
                if (alert != null) {
                    alerts.add(alert);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading alerts: " + e.getMessage());
        }
        return alerts;
    }

    @Override
    public void clear() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }
}
