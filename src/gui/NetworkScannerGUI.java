package gui;

import backend.NetworkScanner;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class NetworkScannerGUI extends JFrame {
    private final JTextField targetField = new JTextField("127.0.0.1", 18);
    private final JComboBox<String> presetBox = new JComboBox<>(new String[]{
        "Common Ports", "Web Services", "Database Ports", "Full 1-1024"
    });
    private final JButton scanButton = new JButton("Start Scan");
    private final JTable table = new JTable();
    private final javax.swing.table.DefaultTableModel model =
        new javax.swing.table.DefaultTableModel(
            new Object[]{"Port", "Service", "Risk", "State", "Latency"}, 0
        );
    private final JProgressBar progress = new JProgressBar();
    private final JLabel status = new JLabel("Ready");
    private final NetworkScanner scanner = new NetworkScanner();

    public NetworkScannerGUI() {
        setTitle("Network & Port Scanner");
        setSize(850, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(new Color(11, 17, 32));
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top.setBackground(new Color(30, 41, 59));
        JLabel target = new JLabel("Target:");
        target.setForeground(Color.WHITE);
        JLabel preset = new JLabel("Preset:");
        preset.setForeground(Color.WHITE);
        top.add(target);
        top.add(targetField);
        top.add(preset);
        top.add(presetBox);
        top.add(scanButton);

        JButton export = new JButton("Export Results");
        export.addActionListener(e -> exportResults());
        top.add(export);

        add(top, BorderLayout.NORTH);

        table.setModel(model);
        table.setRowHeight(24);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new RiskRenderer());
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(10, 5));
        bottom.setBackground(new Color(30, 41, 59));
        progress.setStringPainted(true);
        bottom.add(progress, BorderLayout.CENTER);
        status.setForeground(Color.WHITE);
        bottom.add(status, BorderLayout.WEST);
        add(bottom, BorderLayout.SOUTH);

        scanButton.addActionListener(e -> startScan());
        setVisible(true);
    }

    private void startScan() {
        final String target = targetField.getText().trim();
        if (target.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a target hostname or IP.");
            return;
        }

        final List<Integer> ports;
        String preset = String.valueOf(presetBox.getSelectedItem());
        if ("Web Services".equals(preset)) {
            ports = NetworkScanner.webPorts();
        } else if ("Database Ports".equals(preset)) {
            ports = NetworkScanner.databasePorts();
        } else if ("Full 1-1024".equals(preset)) {
            Integer[] all = new Integer[1024];
            for (int i = 0; i < 1024; i++) all[i] = i + 1;
            ports = Arrays.asList(all);
        } else {
            ports = NetworkScanner.commonPorts();
        }

        model.setRowCount(0);
        scanButton.setEnabled(false);
        progress.setMaximum(ports.size());
        progress.setValue(0);
        status.setText("Scanning " + target + "...");

        new SwingWorker<List<NetworkScanner.PortResult>, NetworkScanner.PortResult>() {
            protected List<NetworkScanner.PortResult> doInBackground() throws Exception {
                return scanner.scan(target, ports, 350, 32);
            }

            protected void done() {
                try {
                    List<NetworkScanner.PortResult> results = get();
                    int openCount = 0;
                    for (NetworkScanner.PortResult result : results) {
                        if (result.isOpen()) {
                            openCount++;
                            model.addRow(new Object[]{
                                result.getPort(),
                                result.getService(),
                                result.getRisk(),
                                "OPEN",
                                result.getLatencyMs() + " ms"
                            });
                        }
                    }
                    progress.setValue(progress.getMaximum());
                    status.setText("Completed: " + openCount + " open ports");
                } catch (Exception ex) {
                    status.setText("Scan failed");
                    JOptionPane.showMessageDialog(
                        NetworkScannerGUI.this,
                        ex.getMessage(),
                        "Scan Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    scanButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void exportResults() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("network-scan.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try (FileWriter writer = new FileWriter(chooser.getSelectedFile())) {
            writer.write("port,service,risk,state,latency_ms\n");
            for (int row = 0; row < model.getRowCount(); row++) {
                writer.write(
                    model.getValueAt(row, 0) + "," +
                    model.getValueAt(row, 1) + "," +
                    model.getValueAt(row, 2) + "," +
                    model.getValueAt(row, 3) + "," +
                    model.getValueAt(row, 4).toString().replace(" ms", "") + "\n"
                );
            }
            status.setText("Results exported");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not export results.", "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class RiskRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(
            JTable table, Object value, boolean selected, boolean focused, int row, int column
        ) {
            Component component = super.getTableCellRendererComponent(table, value, selected, focused, row, column);
            if (!selected && column == 2 && value != null) {
                String risk = value.toString();
                if ("HIGH".equals(risk)) component.setForeground(new Color(239, 68, 68));
                else if ("MEDIUM".equals(risk)) component.setForeground(new Color(245, 158, 11));
                else if ("LOW".equals(risk)) component.setForeground(new Color(16, 185, 129));
                else component.setForeground(new Color(59, 130, 246));
            }
            return component;
        }
    }
}
