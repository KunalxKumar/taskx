package gui;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import backend.*;

public class MainDashboard extends JFrame {
    private final AlertStorage storage = new FileAlertStorage();

    public MainDashboard() {
        setTitle("Cyber Security Suite");
        setSize(500, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        getContentPane().setBackground(new Color(35, 35, 40));
        setLayout(new GridLayout(3, 1, 0, 15));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton btnIDS = createButton("START INTRUSION DETECTION", new Color(70, 130, 180));
        btnIDS.addActionListener(e -> startIDS());

        JButton btnViewer = createButton("OPEN EVENT VIEWER", new Color(60, 179, 113));
        btnViewer.addActionListener(e -> new EventViewerGUI(storage));

        JButton btnKiller = createButton("OPEN PROCESS KILLER", new Color(205, 92, 92));
        btnKiller.addActionListener(e -> new ProcessKillerGUI());

        add(btnIDS);
        add(btnViewer);
        add(btnKiller);

        setVisible(true);
    }

    private JButton createButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = bgColor;

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(
                    Math.min(originalColor.getRed() + 30, 255),
                    Math.min(originalColor.getGreen() + 30, 255),
                    Math.min(originalColor.getBlue() + 30, 255)
                ));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(originalColor);
            }
        });

        return btn;
    }

    private void startIDS() {
        BlockingQueue<model.Event> queue = new LinkedBlockingQueue<>();
        new Thread(new EventCollector(queue)).start();
        new Thread(new Analyzer(queue, storage)).start();
        JOptionPane.showMessageDialog(
            this,
            "Intrusion Detection Engine started in background.\n" +
            "Monitoring events in real-time.\n" +
            "Check Event Viewer for alerts.",
            "IDS Started",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainDashboard::new);
    }
}
