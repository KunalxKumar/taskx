package backend;

import model.Alert;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcAlertStorage implements AlertStorage {
    private final String url;
    private final String user;
    private final String password;

    public JdbcAlertStorage() {
        this(
            System.getProperty("taskx.db.url", "jdbc:mysql://127.0.0.1:3306/taskx?connectTimeout=1000&socketTimeout=1500"),
            System.getProperty("taskx.db.user", "root"),
            System.getProperty("taskx.db.password", "")
        );
    }

    public JdbcAlertStorage(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public boolean isAvailable() {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            ensureTable(connection);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private void ensureTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS alerts (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "timestamp VARCHAR(32) NOT NULL," +
                "severity VARCHAR(16) NOT NULL," +
                "source_ip VARCHAR(64) NOT NULL," +
                "type VARCHAR(64) NOT NULL," +
                "message TEXT NOT NULL)"
            );
        }
    }

    @Override
    public synchronized void save(Alert alert) {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            ensureTable(connection);
            try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO alerts(timestamp,severity,source_ip,type,message) VALUES (?,?,?,?,?)"
            )) {
                statement.setString(1, alert.getTimestamp());
                statement.setString(2, alert.getSeverity());
                statement.setString(3, alert.getSourceIP());
                statement.setString(4, alert.getType());
                statement.setString(5, alert.getMessage());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not save alert to MySQL.", e);
        }
    }

    @Override
    public List<Alert> loadAll() {
        List<Alert> alerts = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            ensureTable(connection);
            try (PreparedStatement statement = connection.prepareStatement(
                "SELECT timestamp,severity,source_ip,type,message FROM alerts ORDER BY id"
            ); ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    Alert alert = new Alert(
                        resultSet.getString("severity"),
                        resultSet.getString("source_ip"),
                        resultSet.getString("type"),
                        resultSet.getString("message")
                    );
                    alert.setTimestamp(resultSet.getString("timestamp"));
                    alerts.add(alert);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load alerts from MySQL.", e);
        }

        return alerts;
    }

    @Override
    public void clear() {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            ensureTable(connection);
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DELETE FROM alerts");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not clear MySQL alerts.", e);
        }
    }
}
