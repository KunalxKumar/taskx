package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import model.ProcessInfo;
import model.ProcessTableModel;

public class ProcessKillerGUI extends JFrame {
    private final ProcessTableModel model;
    private final JTable table;
    private final JLabel statusLabel = new JLabel("Ready");
    private final JTextField searchField = new JTextField(22);
    private final JCheckBox autoRefreshCheck = new JCheckBox("Auto Refresh (2s)", true);
    private final TableRowSorter<ProcessTableModel> sorter;
    private javax.swing.Timer refreshTimer;
    private JTextArea suspiciousArea;
    private final Set<String> whitelist = createWhitelist();

    public ProcessKillerGUI() {
        setTitle("Process Killer - Task Manager");
        setSize(1220, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(new Color(40, 40, 45));

        model = new ProcessTableModel(getProcessList());
        this.table = createProcessTable();
        this.sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createToolbar(), BorderLayout.NORTH);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Running Processes"));
        add(tableScroll, BorderLayout.CENTER);

        add(createBottomPanel(), BorderLayout.SOUTH);

        startAutoRefresh();
        refreshProcessList(false);
        setVisible(true);
    }

    private JPanel createToolbar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panel.setBackground(new Color(50, 50, 55));

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refreshProcessList(true));
        styleButton(btnRefresh, new Color(70, 130, 180));

        JButton btnKill = new JButton("End Task");
        btnKill.addActionListener(e -> killSelectedProcess());
        styleButton(btnKill, new Color(205, 92, 92));

        JButton btnDetect = new JButton("Detect Hidden");
        btnDetect.addActionListener(e -> detectHiddenProcesses());
        styleButton(btnDetect, new Color(255, 140, 0));

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setForeground(Color.WHITE);
        searchField.getDocument().addDocumentListener(new util.SimpleDocumentListener(this::applySearchFilter));

        autoRefreshCheck.setOpaque(false);
        autoRefreshCheck.setForeground(Color.WHITE);
        autoRefreshCheck.addActionListener(e -> toggleAutoRefresh());

        panel.add(btnRefresh);
        panel.add(btnKill);
        panel.add(btnDetect);
        panel.add(searchLabel);
        panel.add(searchField);
        panel.add(autoRefreshCheck);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel container = new JPanel(new BorderLayout(0, 8));
        container.setBackground(new Color(40, 40, 45));

        JPanel suspiciousPanel = createSuspiciousPanel();
        container.add(suspiciousPanel, BorderLayout.CENTER);

        statusLabel.setForeground(new Color(210, 210, 215));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        container.add(statusLabel, BorderLayout.SOUTH);

        return container;
    }

    private void styleButton(JButton btn, Color bgColor) {
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private JTable createProcessTable() {
        JTable t = new JTable(model);
        t.setBackground(new Color(45, 45, 50));
        t.setForeground(Color.WHITE);
        t.setSelectionBackground(new Color(70, 130, 180));
        t.setGridColor(new Color(60, 60, 65));
        t.setFont(new Font("Monospaced", Font.PLAIN, 10));
        t.getTableHeader().setBackground(new Color(50, 50, 55));
        t.getTableHeader().setForeground(Color.WHITE);
        t.setRowHeight(22);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        t.getColumnModel().getColumn(0).setPreferredWidth(90);
        t.getColumnModel().getColumn(1).setPreferredWidth(240);
        t.getColumnModel().getColumn(2).setPreferredWidth(220);
        t.getColumnModel().getColumn(3).setPreferredWidth(140);
        t.getColumnModel().getColumn(4).setPreferredWidth(500);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                super.setValue(value);
                setToolTipText(value == null ? null : value.toString());
            }
        };
        for (int i = 0; i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        return t;
    }

    private JPanel createSuspiciousPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(40, 40, 45));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(205, 92, 92)),
            "Suspicious Processes (Based on Whitelist)",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(205, 92, 92)
        ));
        panel.setPreferredSize(new Dimension(0, 150));

        suspiciousArea = new JTextArea();
        suspiciousArea.setEditable(false);
        suspiciousArea.setBackground(new Color(50, 50, 55));
        suspiciousArea.setForeground(new Color(255, 100, 100));
        suspiciousArea.setFont(new Font("Monospaced", Font.BOLD, 11));
        suspiciousArea.setLineWrap(true);
        suspiciousArea.setWrapStyleWord(true);

        panel.add(new JScrollPane(suspiciousArea), BorderLayout.CENTER);
        return panel;
    }

    private void applySearchFilter() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }

        sorter.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends ProcessTableModel, ? extends Integer> entry) {
                for (int i = 0; i < entry.getValueCount(); i++) {
                    Object value = entry.getValue(i);
                    if (value != null && value.toString().toLowerCase().contains(query)) return true;
                }
                return false;
            }
        });
    }

    private void startAutoRefresh() {
        refreshTimer = new javax.swing.Timer(2000, e -> refreshProcessList(false));
        refreshTimer.start();
    }

    private void toggleAutoRefresh() {
        if (autoRefreshCheck.isSelected()) {
            if (refreshTimer == null || !refreshTimer.isRunning()) startAutoRefresh();
        } else if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }

    private void refreshProcessList(boolean showMessage) {
        Long selectedPid = getSelectedPid();
        List<ProcessInfo> processes = getProcessList();
        processes.sort(Comparator.comparingLong(ProcessInfo::getMemory).reversed());
        model.setProcesses(processes);
        applySearchFilter();
        restoreSelectionByPid(selectedPid);

        statusLabel.setText("Processes: " + processes.size() + " | Updated: " + new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()));
        if (showMessage) {
            JOptionPane.showMessageDialog(this, "Process list refreshed: " + processes.size() + " processes found.");
        }
    }

    private Long getSelectedPid() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        ProcessInfo proc = model.getProcessAt(modelRow);
        return proc == null ? null : proc.getPid();
    }

    private void restoreSelectionByPid(Long pid) {
        if (pid == null) return;
        for (int i = 0; i < model.getRowCount(); i++) {
            ProcessInfo proc = model.getProcessAt(i);
            if (proc != null && proc.getPid() == pid) {
                int viewRow = table.convertRowIndexToView(i);
                if (viewRow >= 0) {
                    table.getSelectionModel().setSelectionInterval(viewRow, viewRow);
                }
                return;
            }
        }
    }

    private List<ProcessInfo> getProcessList() {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return getWindowsProcessList();
        }

        return ProcessHandle.allProcesses()
            .map(ph -> {
                ProcessInfo info = new ProcessInfo();
                info.setPid(ph.pid());
                info.setName(ph.info().command().map(p -> p.substring(p.lastIndexOf(File.separator) + 1)).orElse("Unknown"));
                info.setUser(ph.info().user().orElse("Unknown"));
                info.setMemory(0);
                info.setCommand(ph.info().command().orElse(info.getName()));
                return info;
            }).collect(Collectors.toList());
    }

    private List<ProcessInfo> getWindowsProcessList() {
        List<ProcessInfo> list = new ArrayList<>();
        try {
            String script =
                "$ErrorActionPreference='SilentlyContinue'; " +
                "Get-CimInstance Win32_Process | ForEach-Object { " +
                "$path = if ([string]::IsNullOrWhiteSpace($_.ExecutablePath)) { $_.Name } else { $_.ExecutablePath }; " +
                "$safePath = $path -replace \"`t\", ' '; " +
                "\"{0}`t{1}`t{2}`t{3}`t{4}\" -f $_.ProcessId, $_.Name, 'Unknown', $_.WorkingSetSize, $safePath }";

            ProcessBuilder pb = new ProcessBuilder("powershell", "-NoProfile", "-Command", script);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\t", 5);
                if (parts.length < 4) continue;

                try {
                    ProcessInfo info = new ProcessInfo();
                    info.setPid(Long.parseLong(parts[0].trim()));
                    info.setName(parts[1].trim());
                    info.setUser(parts[2].trim());
                    info.setMemory(Long.parseLong(parts[3].trim()));
                    info.setCommand(parts.length == 5 ? parts[4].trim() : parts[1].trim());
                    list.add(info);
                } catch (NumberFormatException ignored) {
                }
            }
            p.waitFor();
        } catch (Exception e) {
            System.err.println("Error fetching Windows process list: " + e.getMessage());
        }
        if (list.isEmpty()) {
            list = getWindowsProcessListFallback();
        }
        return list;
    }

    private List<ProcessInfo> getWindowsProcessListFallback() {
        List<ProcessInfo> list = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("tasklist", "/FO", "CSV", "/NH");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(p.getInputStream(), java.nio.charset.Charset.defaultCharset())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\",\"");
                if (parts.length < 5) continue;
                try {
                    String name = parts[0].replaceAll("^\"|\"$", "").trim();
                    long pid = Long.parseLong(parts[1].replaceAll("^\"|\"$", "").trim());
                    String memRaw = parts[4].replaceAll("^\"|\"$", "").trim();
                    String digits = memRaw.replaceAll("[^0-9]", "");
                    long memKb = digits.isEmpty() ? 0L : Long.parseLong(digits);

                    ProcessInfo info = new ProcessInfo();
                    info.setPid(pid);
                    info.setName(name);
                    info.setUser("Unknown");
                    info.setMemory(memKb * 1024L);
                    info.setCommand(name);
                    list.add(info);
                } catch (Exception ignored) {
                }
            }
            p.waitFor();
        } catch (Exception e) {
            System.err.println("Fallback tasklist failed: " + e.getMessage());
        }
        return list;
    }

    private static String formatMemory(long bytes) {
        if (bytes <= 0) return "0 KB";
        double kb = bytes / 1024.0;
        if (kb < 1024) return String.format("%.0f KB", kb);
        double mb = kb / 1024.0;
        if (mb < 1024) return String.format("%.1f MB", mb);
        return String.format("%.2f GB", mb / 1024.0);
    }

    private void killSelectedProcess() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a process to end.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        ProcessInfo proc = model.getProcessAt(modelRow);
        if (proc == null) return;

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "End task: " + proc.getName() + " (PID: " + proc.getPid() + ", RAM: " + formatMemory(proc.getMemory()) + ")?",
            "Confirm End Task",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            ProcessHandle.of(proc.getPid()).ifPresentOrElse(ph -> {
                try {
                    ph.destroy();
                    try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                    if (ph.isAlive()) ph.destroyForcibly();
                    refreshProcessList(false);
                    statusLabel.setText("Ended task PID " + proc.getPid() + " (" + proc.getName() + ")");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Failed to end process: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }, () -> JOptionPane.showMessageDialog(this, "Process no longer exists.", "Missing Process", JOptionPane.WARNING_MESSAGE));
        }
    }

    private void detectHiddenProcesses() {
        List<ProcessInfo> allProcesses = model.getProcesses();
        StringBuilder sb = new StringBuilder();

        for (ProcessInfo proc : allProcesses) {
            if (!whitelist.contains(proc.getName().toLowerCase())) {
                sb.append(proc.getName()).append(" (PID: ").append(proc.getPid())
                    .append(", RAM: ").append(formatMemory(proc.getMemory())).append(")")
                    .append(System.lineSeparator());
            }
        }

        suspiciousArea.setText(sb.length() == 0 ? "No suspicious processes detected." : sb.toString());
    }

    private Set<String> createWhitelist() {
        return Set.of(
            "java", "javaw", "explorer.exe", "svchost.exe", "system", "systemd",
            "chrome.exe", "firefox.exe", "notepad.exe", "cmd.exe", "powershell.exe",
            "git", "python", "node", "ruby", "gcc", "clang", "gradle", "maven"
        );
    }

    @Override
    public void dispose() {
        if (refreshTimer != null) refreshTimer.stop();
        super.dispose();
    }
}
