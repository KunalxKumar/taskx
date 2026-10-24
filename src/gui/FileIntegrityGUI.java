package gui;

import backend.FileIntegrityChecker;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.NotDirectoryException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

public class FileIntegrityGUI extends JFrame {
    private final FileIntegrityChecker checker = new FileIntegrityChecker();
    private final JTextField fileField = new JTextField(28);
    private final JComboBox<String> algorithmBox = new JComboBox<>(new String[]{"MD5", "SHA-1", "SHA-256"});
    private final JTextArea resultArea = new JTextArea();
    private final DefaultListModel<String> resultModel = new DefaultListModel<>();

    public FileIntegrityGUI() {
        setTitle("File Integrity Monitor");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(11, 17, 32));

        JPanel top = new JPanel(new GridBagLayout());
        top.setBackground(new Color(30, 41, 59));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0;
        JLabel fileLabel = new JLabel("File / Directory:");
        fileLabel.setForeground(Color.WHITE);
        top.add(fileLabel, c);

        c.gridx = 1;
        top.add(fileField, c);

        c.gridx = 2;
        JButton browse = new JButton("Browse");
        browse.addActionListener(e -> browse());
        top.add(browse, c);

        c.gridx = 0; c.gridy = 1;
        JLabel algorithmLabel = new JLabel("Algorithm:");
        algorithmLabel.setForeground(Color.WHITE);
        top.add(algorithmLabel, c);

        c.gridx = 1;
        top.add(algorithmBox, c);

        c.gridx = 2;
        JButton hash = new JButton("Calculate Hash");
        hash.addActionListener(e -> calculateHash());
        top.add(hash, c);

        c.gridx = 3;
        JButton snapshot = new JButton("Create Snapshot");
        snapshot.addActionListener(e -> createSnapshot());
        top.add(snapshot, c);

        c.gridx = 4;
        JButton verify = new JButton("Verify Snapshot");
        verify.addActionListener(e -> verifySnapshot());
        top.add(verify, c);

        add(top, BorderLayout.NORTH);

        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultArea.setBackground(new Color(15, 23, 42));
        resultArea.setForeground(Color.WHITE);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        JList<String> list = new JList<>(resultModel);
        list.setVisibleRowCount(6);
        add(new JScrollPane(list), BorderLayout.SOUTH);

        JButton copy = new JButton("Copy Result");
        copy.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new java.awt.datatransfer.StringSelection(resultArea.getText()), null);
        });
        top.add(copy, c);

        setVisible(true);
    }

    private void browse() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void calculateHash() {
        File file = new File(fileField.getText().trim());
        if (!file.isFile()) {
            JOptionPane.showMessageDialog(this, "Select a file.");
            return;
        }

        String algorithm = String.valueOf(algorithmBox.getSelectedItem());
        try {
            resultArea.setText(algorithm + ": " + checker.hash(file, algorithm) + "
");
        } catch (Exception e) {
            resultArea.setText("Hash error: " + e.getMessage());
        }
    }

    private void createSnapshot() {
        File directory = new File(fileField.getText().trim());
        if (!directory.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Select a directory.");
            return;
        }

        String algorithm = String.valueOf(algorithmBox.getSelectedItem());
        try {
            Map<String, String> manifest = checker.snapshot(directory, algorithm);
            File output = new File(directory, "taskx-" + algorithm.toLowerCase().replace("-", "") + ".manifest");
            checker.writeManifest(manifest, output);
            resultArea.setText("Snapshot: " + output.getAbsolutePath() + "
Files: " + manifest.size());
        } catch (Exception e) {
            resultArea.setText("Snapshot error: " + e.getMessage());
        }
    }

    private void verifySnapshot() {
        File directory = new File(fileField.getText().trim());
        if (!directory.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Select a directory.");
            return;
        }

        JFileChooser chooser = new JFileChooser(directory);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try {
            Map<String, String> baseline = readManifest(chooser.getSelectedFile());
            String algorithm = String.valueOf(algorithmBox.getSelectedItem());
            List<FileIntegrityChecker.VerificationResult> results =
                checker.verify(baseline, directory, algorithm);

            resultModel.clear();
            for (FileIntegrityChecker.VerificationResult result : results) {
                resultModel.addElement(result.getStatus() + " | " + result.getPath());
            }
            resultArea.setText("Verification complete. Entries: " + results.size());
        } catch (Exception e) {
            resultArea.setText("Verification error: " + e.getMessage());
        }
    }

    private Map<String, String> readManifest(File manifest) throws Exception {
        Map<String, String> map = new java.util.TreeMap<>();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(manifest))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int split = line.indexOf("  ");
                if (split > 0 && split + 2 < line.length()) {
                    map.put(line.substring(split + 2), line.substring(0, split));
                }
            }
        }
        return map;
    }
}
