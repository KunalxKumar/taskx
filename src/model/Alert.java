package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Alert {
    private String timestamp;
    private String severity;
    private String sourceIP;
    private String type;
    private String message;

    public Alert(String severity, String sourceIP, String type, String message) {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.severity = severity;
        this.sourceIP = sourceIP;
        this.type = type;
        this.message = message;
    }

    // Getters
    public String getTimestamp() { return timestamp; }
    public String getSeverity() { return severity; }
    public String getSourceIP() { return sourceIP; }
    public String getType() { return type; }
    public String getMessage() { return message; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    /** Serializes to one line for logging */
    public String toLogLine() {
        return timestamp + "|" + severity + "|" + sourceIP + "|" + type + "|" + message;
    }

    /** Deserializes from a log line */
    public static Alert fromLogLine(String line) {
        String[] parts = line.split("\\|", 5);
        if (parts.length < 5) return null;
        Alert a = new Alert(parts[1], parts[2], parts[3], parts[4]);
        a.timestamp = parts[0];   // override the auto‑generated timestamp
        return a;
    }

    @Override
    public String toString() {
        return "[" + severity + "] " + timestamp + " - " + message + " (IP: " + sourceIP + ")";
    }
}