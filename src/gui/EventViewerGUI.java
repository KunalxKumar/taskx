package gui;

import javax.swing.*;
import java.awt.*;
import model.Alert;
import model.AlertTableModel;
import backend.AlertStorage;
import util.SimpleDocumentListener;

public class EventViewerGUI extends JFrame {
    private final AlertStorage storage;
    private final AlertTableModel model;
    private final JTable table;
    private JTextArea detailArea;
    private Timer refreshTimer;

    public EventViewerGUI(AlertStorage storage) {
        this.storage = storage;

        setTitle("Event Viewer - Alert Management");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(new Color(40, 40, 45));

        model = new AlertTableModel(storage.loadAll());

        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        add(createFilterPanel(), BorderLayout.NORTH);

        table = createAlertsTable();
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Alerts"));
        add(tableScroll, BorderLayout.CENTER);

        add(createDetailPanel(), BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> updateDetails());

        refreshTimer = new Timer(4000, e -> refreshAlerts(false));
        refreshTimer.start();

        setVisible(true);
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBackground(new Color(50, 50, 55));
        panel.setBorder(BorderFactory.createTitledBorder("Filters"));

        JLabel lblSeverity = new JLabel("Severity:");
        lblSeverity.setForeground(Color.WHITE);
        String[] severities = {"ALL", "HIGH", "MEDIUM", "LOW"};
        JComboBox<String> severityBox = new JComboBox<>(severities);
        severityBox.setBackground(new Color(60, 60, 65));
        severityBox.setForeground(Color.WHITE);
        severityBox.addActionListener(e -> {
            String selected = (String) severityBox.getSelectedItem();
            if ("ALL".equals(selected)) {
                model.clearSeverityFilter();
            } else {
                model.filterBySeverity(selected);
            }
        });

        JLabel lblIP = new JLabel("Source IP:");
        lblIP.setForeground(Color.WHITE);
        JTextField ipField = new JTextField(15);
        ipField.setBackground(new Color(60, 60, 65));
        ipField.setForeground(Color.WHITE);
        ipField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            String ip = ipField.getText().trim();
            if (ip.isEmpty()) {
                model.clearIPFilter();
            } else {
                model.filterByIP(ip);
            }
        }));

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setForeground(Color.WHITE);
        JTextField searchField = new JTextField(20);
        searchField.setBackground(new Color(60, 60, 65));
        searchField.setForeground(Color.WHITE);
        searchField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            String query = searchField.getText().trim();
            if (query.isEmpty()) {
                model.clearSearch();
            } else {
                model.search(query);
            }
        }));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(70, 130, 180));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> refreshAlerts(true));

        panel.add(lblSeverity);
        panel.add(severityBox);
        panel.add(lblIP);
        panel.add(ipField);
        panel.add(lblSearch);
        panel.add(searchField);
        panel.add(refreshButton);

        return panel;
    }

    private JTable createAlertsTable() {
        JTable t = new JTable(model);
        t.setBackground(new Color(45, 45, 50));
        t.setForeground(Color.WHITE);
        t.setSelectionBackground(new Color(70, 130, 180));
        t.setSelectionForeground(Color.WHITE);
        t.setGridColor(new Color(60, 60, 65));
        t.setFont(new Font("Monospaced", Font.PLAIN, 11));
        t.getTableHeader().setBackground(new Color(50, 50, 55));
        t.getTableHeader().setForeground(Color.WHITE);
        t.setRowHeight(25);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        return t;
    }

    private JPanel createDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(40, 40, 45));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "Alert Details",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 12),
            Color.WHITE
        ));

        detailArea = new JTextArea(5, 80);
        detailArea.setEditable(false);
        detailArea.setBackground(new Color(50, 50, 55));
        detailArea.setForeground(Color.WHITE);
        detailArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(detailArea);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void updateDetails() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            detailArea.setText("");
            return;
        }

        Alert alert = model.getAlertAt(selectedRow);
        if (alert != null) {
            String details = String.format(
                "Timestamp: %s%n" +
                "Severity: %s%n" +
                "Source IP: %s%n" +
                "Type: %s%n" +
                "Message: %s",
                alert.getTimestamp(),
                alert.getSeverity(),
                alert.getSourceIP(),
                alert.getType(),
                alert.getMessage()
            );
            detailArea.setText(details);
        }
    }

    private void refreshAlerts(boolean showMessage) {
        java.util.List<Alert> alerts = storage.loadAll();
        model.setAlerts(alerts);
        if (showMessage) {
            JOptionPane.showMessageDialog(this, "Alerts refreshed: " + alerts.size() + " records loaded.");
        }
    }

    @Override
    public void dispose() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        super.dispose();
    }
}
