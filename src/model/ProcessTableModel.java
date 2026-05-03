package model;

import javax.swing.table.*;
import java.util.*;

public class ProcessTableModel extends AbstractTableModel {
    private List<ProcessInfo> processes;
    private final String[] columns = {"PID", "Process Name", "User", "Memory", "Command"};

    public ProcessTableModel(List<ProcessInfo> processes) {
        this.processes = new ArrayList<>(processes);
    }

    @Override
    public int getRowCount() {
        return processes.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (row < 0 || row >= processes.size()) return null;
        ProcessInfo proc = processes.get(row);

        return switch (column) {
            case 0 -> proc.getPid();
            case 1 -> proc.getName();
            case 2 -> proc.getUser();
            case 3 -> formatMemory(proc.getMemory());
            case 4 -> proc.getCommand();
            default -> null;
        };
    }

    public void setProcesses(List<ProcessInfo> newProcesses) {
        this.processes = new ArrayList<>(newProcesses);
        fireTableDataChanged();
    }

    public List<ProcessInfo> getProcesses() {
        return processes;
    }

    public ProcessInfo getProcessAt(int row) {
        if (row < 0 || row >= processes.size()) return null;
        return processes.get(row);
    }

    private static String formatMemory(long bytes) {
        if (bytes <= 0) return "0 KB";
        long kb = bytes / 1024;
        if (kb < 1024) return kb + " KB";
        if (kb < 1024 * 1024) return String.format("%.1f MB", kb / 1024.0);
        return String.format("%.2f GB", kb / 1024.0 / 1024.0);
    }
}
