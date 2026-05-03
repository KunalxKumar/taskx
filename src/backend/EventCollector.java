package backend;

import model.Event;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class EventCollector implements Runnable {
    private final BlockingQueue<Event> queue;
    private final Random random = new Random();

    public EventCollector(BlockingQueue<Event> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        System.out.println("Event Collector started.");
        String[] eventTypes = {"LOGIN_ATTEMPT", "FILE_ACCESS", "NETWORK_CONNECTION", "PROCESS_START"};
        String[] ips = {"192.168.1.10", "10.0.0.5", "172.16.0.2", "8.8.8.8"};

        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Simulate event arrival
                Thread.sleep(2000 + random.nextInt(3000));

                String type = eventTypes[random.nextInt(eventTypes.length)];
                String ip = ips[random.nextInt(ips.length)];
                String description = generateDescription(type);

                Event event = new Event(type, description, ip);
                queue.put(event);
                System.out.println("Collected: " + event);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private String generateDescription(String type) {
        switch (type) {
            case "LOGIN_ATTEMPT":
                return random.nextBoolean() ? "Successful login for user admin" : "Failed login attempt for user root";
            case "FILE_ACCESS":
                return "Accessed /etc/shadow";
            case "NETWORK_CONNECTION":
                return "Connection established to port 443";
            case "PROCESS_START":
                return "Started process: " + (random.nextBoolean() ? "cmd.exe" : "powershell.exe");
            default:
                return "Generic system event";
        }
    }
}
