package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Event {
    private String type;
    private String description;
    private String sourceIP;
    private String timestamp;

    public Event(String type, String description, String sourceIP) {
        this.type = type;
        this.description = description;
        this.sourceIP = sourceIP;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getType() { return type; }
    public String getDescription() { return description; }
    public String getSourceIP() { return sourceIP; }
    public String getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s (Source: %s)", timestamp, type, description, sourceIP);
    }
}
