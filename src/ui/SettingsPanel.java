package ui;

import backend.SettingsManager;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import database.AppSettingsDAO;
import events.TransactionEventManager;
import backend.DataExportImport;
import service.GeminiService;

public class SettingsPanel extends JPanel {
    private AppSettingsDAO settingsDAO;
    private DataExportImport dataExportImport;
    private JTextField nameField;
    private JComboBox<String> currencyComboBox;
    private JTextField apiKeyField;
    private JComboBox<String> llmProviderComboBox;
    private Main mainFrame;
    
    // Theme colors
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 30);
    private static final Color PANEL_COLOR = new Color(42, 42, 42);
    private static final Color ACCENT_COLOR = new Color(0, 200, 151);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(60, 60, 60);
    
    public SettingsPanel(Main mainFrame) {
        this.mainFrame = mainFrame;
        settingsDAO = new AppSettingsDAO();
        dataExportImport = new DataExportImport();
        
        setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        initComponents();
        loadSettings();
    }
    
    private void initComponents() {
        // Header
        JLabel headerLabel = new JLabel("⚙️ Settings");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        headerLabel.setForeground(TEXT_COLOR);
        add(headerLabel, BorderLayout.NORTH);
        
        // Main content
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        
        // User preferences panel
        JPanel preferencesPanel = createPreferencesPanel();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(20, 0, 20, 0);
        mainPanel.add(preferencesPanel, gbc);
        
        // Data management panel
        JPanel dataManagementPanel = createDataManagementPanel();
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(dataManagementPanel, gbc);
        
        // About panel
        JPanel aboutPanel = createAboutPanel();
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        mainPanel.add(aboutPanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createPreferencesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Title
        JLabel titleLabel = new JLabel("User Preferences");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(titleLabel, gbc);
        
        // Name field
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(createLabel("Display Name:"), gbc);
        
        nameField = createTextField();
        nameField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(nameField, gbc);
        
        // Currency selection
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(createLabel("Currency:"), gbc);
        
        currencyComboBox = new JComboBox<>(new String[]{
            "INR (₹)", "USD ($)", "EUR (€)", "GBP (£)"
        });
        styleComboBox(currencyComboBox);
        currencyComboBox.setPreferredSize(new Dimension(150, 30));
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(currencyComboBox, gbc);
        
        // LLM Provider selection
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(createLabel("AI Provider:"), gbc);
        
        llmProviderComboBox = new JComboBox<>(new String[]{
            "Gemini", "OpenAI", "Claude", "Mock (Testing)"
        });
        styleComboBox(llmProviderComboBox);
        llmProviderComboBox.setPreferredSize(new Dimension(150, 30));
        gbc.gridx = 1; gbc.gridy = 3;
        panel.add(llmProviderComboBox, gbc);
        
        // Gemini API Key field
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(createLabel("API Key:"), gbc);
        
        apiKeyField = createTextField();
        apiKeyField.setPreferredSize(new Dimension(250, 30));
        gbc.gridx = 1; gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(apiKeyField, gbc);
        
        // Info label for API key
        JLabel apiInfoLabel = new JLabel("<html><i style='font-size: 10px; color: #888;'>Get your Gemini key from: https://makersuite.google.com/app/apikey</i></html>");
        gbc.gridx = 1; gbc.gridy = 5;
        gbc.insets = new Insets(0, 10, 10, 10);
        panel.add(apiInfoLabel, gbc);
        
        // Reset insets for save button
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Save button
        JButton saveButton = createStyledButton("💾 Save Settings");
        saveButton.setPreferredSize(new Dimension(180, 40));
        saveButton.addActionListener(e -> saveSettings());
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(30, 10, 10, 10);
        panel.add(saveButton, gbc);
        
        return panel;
    }
    
    private JPanel createDataManagementPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Title
        JLabel titleLabel = new JLabel("Data Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 15, 0);
        panel.add(titleLabel, gbc);
        
        // Clear transactions button
        JButton clearTransactionsBtn = createStyledButton("🗑️ Clear All Transactions");
        clearTransactionsBtn.setBackground(new Color(255, 152, 0));
        clearTransactionsBtn.addActionListener(e -> clearAllTransactions());
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        panel.add(clearTransactionsBtn, gbc);
        
        // Reset all data button
        JButton resetAllBtn = createStyledButton("⚠️ Reset All Data");
        resetAllBtn.setBackground(new Color(244, 67, 54));
        resetAllBtn.addActionListener(e -> resetAllData());
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(resetAllBtn, gbc);
        
        // Export data button
        JButton exportBtn = createStyledButton("💾 Export Data (CSV)");
        exportBtn.setBackground(new Color(33, 150, 243));
        exportBtn.addActionListener(e -> exportData());
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 10, 5);
        panel.add(exportBtn, gbc);
        
        // Import data button
        JButton importBtn = createStyledButton("💾 Import Data (CSV)");
        importBtn.setBackground(new Color(76, 175, 80));
        importBtn.addActionListener(e -> importData());
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.insets = new Insets(10, 5, 10, 10);
        panel.add(importBtn, gbc);
        
        // Info label
        JLabel infoLabel = new JLabel("<html>⚠️ Warning: These actions cannot be undone!<br/>" +
                                      "Clear Transactions: Removes all financial records but keeps settings.<br/>" +
                                      "Reset All Data: Clears transactions and resets budget to defaults.</html>");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoLabel.setForeground(new Color(255, 193, 7));
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 10, 0, 10);
        panel.add(infoLabel, gbc);
        
        return panel;
    }
    
    private JPanel createAboutPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Title
        JLabel titleLabel = new JLabel("About FinSight");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(titleLabel, gbc);
        
        // App info
        String aboutText = """
            <html>
            <div style='text-align: center; color: white;'>
            <h3 style='color: #00C897;'>💰 Personal Finance Advisor v1.0</h3>
            <p>A comprehensive financial management tool built with Java Swing</p>
            <br>
            <p><b>Features:</b></p>
            <p>• Transaction tracking and management</p>
            <p>• Budget planning and monitoring</p>
            <p>• AI-powered financial recommendations</p>
            <p>• Intuitive dark theme interface</p>
            <br>
            <p><b>Technology Stack:</b></p>
            <p>• Java Swing for UI</p>
            <p>• SQLite for data storage</p>
            <p>• Modern dark theme design</p>
            <br>
            <p style='font-size: 12px; color: #888888;'>
            Built with ❤️ for better financial management
            </p>
            </div>
            </html>
            """;
        
        JLabel aboutLabel = new JLabel(aboutText);
        aboutLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        panel.add(aboutLabel, gbc);
        
        return panel;
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_COLOR);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return label;
    }
    
    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setBackground(PANEL_COLOR.brighter());
        field.setForeground(TEXT_COLOR);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }
    
    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setBackground(PANEL_COLOR.brighter());
        comboBox.setForeground(TEXT_COLOR);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboBox.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
    }
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(TEXT_COLOR);
        button.setBackground(ACCENT_COLOR);
        button.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        button.setFocusPainted(false);
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(ACCENT_COLOR.brighter());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(ACCENT_COLOR);
            }
        });
        
        return button;
    }
    
    private void saveSettings() {
        String name = nameField.getText().trim();
        String currencyDisplay = (String) currencyComboBox.getSelectedItem();
        
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a display name.", 
                "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Extract currency code from display string
        String currencyCode = "INR"; // Default
        if (currencyDisplay.startsWith("USD")) {
            currencyCode = "USD";
        } else if (currencyDisplay.startsWith("EUR")) {
            currencyCode = "EUR";
        } else if (currencyDisplay.startsWith("GBP")) {
            currencyCode = "GBP";
        } else if (currencyDisplay.startsWith("INR")) {
            currencyCode = "INR";
        }
        
        // Get API key and LLM provider
        String apiKey = apiKeyField.getText().trim();
        String llmProvider = (String) llmProviderComboBox.getSelectedItem();
        
        // Save to database and SettingsManager
        settingsDAO.setSetting("user_name", name);
        SettingsManager.setCurrency(currencyCode);
        
        // Save AI settings to database
        if (!apiKey.isEmpty()) {
            settingsDAO.setSetting("gemini_api_key", apiKey);
            
            // Also save to config.properties for GeminiService
            if ("Gemini".equals(llmProvider)) {
                if (GeminiService.saveApiKey(apiKey)) {
                    System.out.println("Gemini API key saved to config.properties");
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Warning: Failed to save API key to config file.\nPlease check file permissions.",
                        "Config Save Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
        settingsDAO.setSetting("llm_provider", llmProvider);
        
        JOptionPane.showMessageDialog(this, 
            "Settings saved successfully!\nCurrency: " + currencyDisplay + "\nAI Provider: " + llmProvider,
            "Settings Saved", JOptionPane.INFORMATION_MESSAGE);
        
        // Refresh all panels to show new currency and reload AI config
        if (mainFrame != null) {
            mainFrame.refreshAllPanels();
            
            // Notify user to refresh AI panel if API key was set
            if (!apiKey.isEmpty() && "Gemini".equals(llmProvider)) {
                JOptionPane.showMessageDialog(this, 
                    "✅ Gemini API key configured!\n\nGo to the AI Advisor panel and click 'Refresh Context' to start using AI features.",
                    "AI Ready", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private void loadSettings() {
        String savedName = settingsDAO.getSetting("user_name", "User");
        String currencyCode = SettingsManager.getCurrencyCode();
        String apiKey = settingsDAO.getSetting("gemini_api_key", "");
        String llmProvider = settingsDAO.getSetting("llm_provider", "Gemini");
        
        nameField.setText(savedName);
        apiKeyField.setText(apiKey);
        
        // Select the correct currency in dropdown
        switch (currencyCode) {
            case "INR":
                currencyComboBox.setSelectedItem("INR (₹)");
                break;
            case "USD":
                currencyComboBox.setSelectedItem("USD ($)");
                break;
            case "EUR":
                currencyComboBox.setSelectedItem("EUR (€)");
                break;
            case "GBP":
                currencyComboBox.setSelectedItem("GBP (£)");
                break;
            default:
                currencyComboBox.setSelectedItem("INR (₹)");
        }
        
        // Select the correct LLM provider
        llmProviderComboBox.setSelectedItem(llmProvider);
    }
    
    private void clearAllTransactions() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete all transactions?\nThis action cannot be undone!",
            "Confirm Clear Transactions", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (settingsDAO.deleteAllTransactions()) {
                // Notify all listeners to refresh
                TransactionEventManager.getInstance().notifyTransactionsRefreshed();
                JOptionPane.showMessageDialog(this, "All transactions have been deleted.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete transactions.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void resetAllData() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to reset ALL data?\n\nThis will:\n" +
            "  • Delete all transactions\n" +
            "  • Reset budget to defaults\n" +
            "  • Clear category budgets\n\n" +
            "This action CANNOT be undone!",
            "Confirm Reset All Data", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (settingsDAO.resetAllData()) {
                // Notify all listeners to refresh
                TransactionEventManager.getInstance().notifyTransactionsRefreshed();
                JOptionPane.showMessageDialog(this, "All data has been reset to defaults.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to reset data.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void exportData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        fileChooser.setSelectedFile(new File(dataExportImport.createBackupFilename()));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (dataExportImport.exportToCSV(file.getAbsolutePath())) {
                JOptionPane.showMessageDialog(this,
                    "Data exported successfully to:\n" + file.getAbsolutePath(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to export data.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void importData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "Import data from: " + file.getName() + "?\n\n" +
                "This will add imported transactions to your existing data.",
                "Confirm Import", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                if (dataExportImport.importFromCSV(file.getAbsolutePath())) {
                    // Notify all listeners to refresh
                    TransactionEventManager.getInstance().notifyTransactionsRefreshed();
                    JOptionPane.showMessageDialog(this,
                        "Data imported successfully from:\n" + file.getAbsolutePath(),
                        "Import Successful", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to import data.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
