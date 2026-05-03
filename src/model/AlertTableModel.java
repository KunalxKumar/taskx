package model;

import javax.swing.table.*;
import java.util.*;
import java.util.stream.*;

public class AlertTableModel extends AbstractTableModel {
    private List<Alert> allAlerts;
    private List<Alert> filteredAlerts;
    private final String[] columns = {"Timestamp", "Severity", "Source IP", "Type", "Message"};
    private String severityFilter = "ALL";
    private String ipFilter = "";
    private String searchQuery = "";

    public AlertTableModel(List<Alert> alerts) {
        this.allAlerts = new ArrayList<>(alerts);
        this.filteredAlerts = new ArrayList<>(alerts);
    }

    @Override
    public int getRowCount() {
        return filteredAlerts.size();
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
        if (row < 0 || row >= filteredAlerts.size()) return null;
        Alert alert = filteredAlerts.get(row);

        return switch (column) {
            case 0 -> alert.getTimestamp();
            case 1 -> alert.getSeverity();
            case 2 -> alert.getSourceIP();
            case 3 -> alert.getType();
            case 4 -> alert.getMessage();
            default -> null;
        };
    }

    public void filterBySeverity(String severity) {
        severityFilter = severity;
        applyFilters();
    }

    public void clearSeverityFilter() {
        severityFilter = "ALL";
        applyFilters();
    }

    public void filterByIP(String ip) {
        ipFilter = ip == null ? "" : ip.trim().toLowerCase();
        applyFilters();
    }

    public void clearIPFilter() {
        ipFilter = "";
        applyFilters();
    }

    public void search(String query) {
        searchQuery = query == null ? "" : query.trim().toLowerCase();
        applyFilters();
    }

    public void clearSearch() {
        searchQuery = "";
        applyFilters();
    }

    public void setAlerts(List<Alert> alerts) {
        this.allAlerts = new ArrayList<>(alerts);
        applyFilters();
    }

    public Alert getAlertAt(int row) {
        if (row < 0 || row >= filteredAlerts.size()) return null;
        return filteredAlerts.get(row);
    }

    private void applyFilters() {
        filteredAlerts = allAlerts.stream()
            .filter(a -> "ALL".equalsIgnoreCase(severityFilter) || a.getSeverity().equalsIgnoreCase(severityFilter))
            .filter(a -> ipFilter.isEmpty() || a.getSourceIP().toLowerCase().contains(ipFilter))
            .filter(a -> searchQuery.isEmpty()
                || a.getMessage().toLowerCase().contains(searchQuery)
                || a.getSourceIP().toLowerCase().contains(searchQuery)
                || a.getType().toLowerCase().contains(searchQuery)
                || a.getSeverity().toLowerCase().contains(searchQuery))
            .collect(Collectors.toList());
        fireTableDataChanged();
    }
}
