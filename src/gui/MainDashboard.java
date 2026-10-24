package gui;

import backend.*;
import javax.swing.*;
import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MainDashboard extends JFrame {
    private final AlertStorage storage = new ResilientAlertStorage();
    private final JLabel storageStatus = new JLabel();
    private final JLabel telemetry = new JLabel();

    public MainDashboard() {
        setTitle("TaskX Cyber Security Suite");
        setSize(920, 620);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        getContentPane().setBackground(new Color(11, 17, 32));
        setLayout(new BorderLayout(16, 16));
        ((JPanel) getContentPane()).setBorder(
            BorderFactory.createEmptyBorder(20, 20, 16, 20)
        );

        add(createHeader(), BorderLayout.NORTH);
        add(createCards(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        new Timer(1500, e -> updateTelemetry()).start();
        setVisible(true);
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(11, 17, 32));

        JLabel title = new JLabel("TASKX");
        title.setForeground(new Color(0, 240, 255));
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));

        JLabel subtitle = new JLabel("Cyber Security Suite  •  v2.0");
        subtitle.setForeground(Color.WHITE);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        JPanel text = new JPanel(new GridLayout(2, 1));
        text.setOpaque(false);
        text.add(title);
        text.add(subtitle);
        panel.add(text, BorderLayout.WEST);

        JLabel badge = new JLabel("LOCAL SECURITY HUB");
        badge.setForeground(new Color(16, 185, 129));
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(badge, BorderLayout.EAST);

        return panel;
    }

    private JPanel createCards() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 14, 14));
        panel.setOpaque(false);

        panel.add(createCard(
            "Intrusion Detection",
            "Monitor and analyze security events in real time.",
            new Color(59, 130, 246),
            this::startIDS
        ));
        panel.add(createCard(
            "Event Viewer",
            "Review, filter and export security alerts.",
            new Color(16, 185, 129),
            () -> new EventViewerGUI(storage)
        ));
        panel.add(createCard(
            "Process Killer",
            "Inspect Windows processes and terminate unsafe tasks.",
            new Color(239, 68, 68),
            () -> new ProcessKillerGUI()
        ));
        panel.add(createCard(
            "Network Scanner",
            "Scan authorized hosts for exposed TCP services.",
            new Color(0, 240, 255),
            () -> new NetworkScannerGUI()
        ));
        panel.add(createCard(
            "File Integrity",
            "Hash files and detect changes in directory snapshots.",
            new Color(245, 158, 11),
            () -> new FileIntegrityGUI()
        ));

        JPanel info = new JPanel();
        info.setOpaque(false);
        panel.add(info);

        return panel;
    }

    private JPanel createCard(String title, String description, Color accent, Runnable action) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(new Color(30, 41, 59));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85)),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel heading = new JLabel(title);
        heading.setForeground(accent);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JTextArea body = new JTextArea(description);
        body.setEditable(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setOpaque(false);
        body.setForeground(new Color(203, 213, 225));
        body.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton launch = new JButton("OPEN");
        launch.setForeground(Color.WHITE);
        launch.setBackground(new Color(15, 23, 42));
        launch.setFocusPainted(false);
        launch.setBorder(BorderFactory.createLineBorder(accent));
        launch.addActionListener(e -> action.run());

        card.add(heading, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(launch, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout(10, 6));
        footer.setBackground(new Color(15, 23, 42));
        footer.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        telemetry.setForeground(new Color(203, 213, 225));
        storageStatus.setForeground(new Color(16, 185, 129));

        footer.add(telemetry, BorderLayout.WEST);
        footer.add(storageStatus, BorderLayout.EAST);
        updateTelemetry();
        return footer;
    }

    private void updateTelemetry() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        telemetry.setText(
            "Java " + System.getProperty("java.version") +
            "  •  " + System.getProperty("os.name") +
            "  •  Threads " + bean.getThreadCount()
        );
        storageStatus.setText(
            storage instanceof ResilientAlertStorage &&
            ((ResilientAlertStorage) storage).isUsingDatabase()
                ? "Storage: MySQL"
                : "Storage: Local File"
        );
    }

    private void startIDS() {
        BlockingQueue<model.Event> queue = new LinkedBlockingQueue<>();
        new Thread(new EventCollector(queue), "TaskX-EventCollector").start();
        new Thread(new Analyzer(queue, storage), "TaskX-Analyzer").start();
        JOptionPane.showMessageDialog(
            this,
            "Intrusion Detection Engine started in background.\nCheck Event Viewer for generated alerts.",
            "IDS Started",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainDashboard::new);
    }
}
