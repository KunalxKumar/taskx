package backend;

import model.Alert;
import model.Event;
import java.util.concurrent.BlockingQueue;

public class Analyzer implements Runnable {
    private final BlockingQueue<Event> queue;
    private final AlertStorage storage;

    public Analyzer(BlockingQueue<Event> queue, AlertStorage storage) {
        this.queue = queue;
        this.storage = storage;
    }

    @Override
    public void run() {
        System.out.println("Analyzer started.");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Event event = queue.take();
                analyze(event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void analyze(Event event) {
        // Simple rule-based analysis
        String severity = "LOW";
        String message = "";

        if (event.getType().equals("LOGIN_ATTEMPT") && event.getDescription().contains("Failed")) {
            severity = "HIGH";
            message = "Brute force suspicion: " + event.getDescription();
        } else if (event.getType().equals("FILE_ACCESS") && event.getDescription().contains("/etc/shadow")) {
            severity = "HIGH";
            message = "Sensitive file access: " + event.getDescription();
        } else if (event.getType().equals("PROCESS_START") && (event.getDescription().contains("powershell") || event.getDescription().contains("cmd"))) {
            severity = "MEDIUM";
            message = "Shell execution detected: " + event.getDescription();
        } else if (event.getType().equals("NETWORK_CONNECTION")) {
            severity = "LOW";
            message = "External connection: " + event.getDescription();
        }

        if (!severity.equals("LOW") || Math.random() > 0.7) { // Alert on all non-LOW or 30% of LOW
            Alert alert = new Alert(severity, event.getSourceIP(), event.getType(), message.isEmpty() ? event.getDescription() : message);
            storage.save(alert);
            System.out.println("Alert generated: " + alert);
        }
    }
}
