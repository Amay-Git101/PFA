import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import ui.*;

public class Main extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private JButton selectedButton;
    
    // Theme colors
    public static final Color BACKGROUND_COLOR = new Color(30, 30, 30);      // #1E1E1E
    public static final Color PANEL_COLOR = new Color(42, 42, 42);           // #2A2A2A
    public static final Color ACCENT_COLOR = new Color(0, 200, 151);         // #00C897
    public static final Color HOVER_COLOR = new Color(0, 230, 168);          // #00E6A8
    public static final Color TEXT_COLOR = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(60, 60, 60);
    
    public Main() {
        setTitle("Personal Finance Advisor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // Set dark theme
        setupDarkTheme();
        
        // Initialize layout
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Create sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);
        
        // Create main content area
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(BACKGROUND_COLOR);
        
        // Add panels
        mainContentPanel.add(new DashboardPanel(), "Dashboard");
        mainContentPanel.add(new AiPanel(), "AI Advisor");
        mainContentPanel.add(new InvestmentPanel(), "Investment Management");
        mainContentPanel.add(new ExpensePanel(), "Expenses");
        mainContentPanel.add(new BudgetPanel(), "Budget");
        mainContentPanel.add(new ReportsPanel(), "Reports");
        mainContentPanel.add(new SettingsPanel(), "Settings");
        
        add(mainContentPanel, BorderLayout.CENTER);
        
        // Show dashboard by default
        cardLayout.show(mainContentPanel, "Dashboard");
    }
    
    private void setupDarkTheme() {
        // Use Metal Look and Feel for better control
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Customize UI components with explicit colors
        UIManager.put("Panel.background", BACKGROUND_COLOR);
        UIManager.put("Panel.foreground", TEXT_COLOR);
        
        UIManager.put("Button.background", PANEL_COLOR);
        UIManager.put("Button.foreground", TEXT_COLOR);
        UIManager.put("Button.border", new LineBorder(BORDER_COLOR, 1, true));
        
        UIManager.put("Label.foreground", TEXT_COLOR);
        UIManager.put("Label.background", BACKGROUND_COLOR);
        
        UIManager.put("TextField.background", PANEL_COLOR);
        UIManager.put("TextField.foreground", TEXT_COLOR);
        UIManager.put("TextField.caretForeground", TEXT_COLOR);
        UIManager.put("TextField.border", new LineBorder(BORDER_COLOR, 1, true));
        
        UIManager.put("ComboBox.background", PANEL_COLOR);
        UIManager.put("ComboBox.foreground", TEXT_COLOR);
        UIManager.put("ComboBox.buttonBackground", PANEL_COLOR);
        UIManager.put("ComboBox.buttonShadow", BORDER_COLOR);
        
        UIManager.put("Table.background", PANEL_COLOR);
        UIManager.put("Table.foreground", TEXT_COLOR);
        UIManager.put("Table.gridColor", BORDER_COLOR);
        UIManager.put("Table.selectionBackground", ACCENT_COLOR);
        UIManager.put("Table.selectionForeground", BACKGROUND_COLOR);
        
        UIManager.put("TableHeader.background", BACKGROUND_COLOR);
        UIManager.put("TableHeader.foreground", TEXT_COLOR);
        
        UIManager.put("ScrollPane.background", PANEL_COLOR);
        UIManager.put("ScrollPane.foreground", TEXT_COLOR);
        UIManager.put("Viewport.background", PANEL_COLOR);
        UIManager.put("Viewport.foreground", TEXT_COLOR);
        
        UIManager.put("TextArea.background", PANEL_COLOR);
        UIManager.put("TextArea.foreground", TEXT_COLOR);
        UIManager.put("TextArea.caretForeground", TEXT_COLOR);
        
        UIManager.put("ProgressBar.background", PANEL_COLOR);
        UIManager.put("ProgressBar.foreground", ACCENT_COLOR);
        
        UIManager.put("OptionPane.background", BACKGROUND_COLOR);
        UIManager.put("OptionPane.messageForeground", TEXT_COLOR);
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(PANEL_COLOR);
        sidebar.setPreferredSize(new Dimension(220, getHeight()));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        
        // Add title
        JLabel titleLabel = new JLabel("Finance Advisor");
        titleLabel.setForeground(ACCENT_COLOR);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(titleLabel);
        sidebar.add(Box.createVerticalStrut(30));
        
        // Create navigation buttons
        String[] menuItems = {"Dashboard", "AI Advisor", "Investment Management", "Expenses", "Budget", "Reports", "Settings"};
        String[] icons = {"🏠", "🤖", "📈", "💰", "📊", "📉", "⚙️"};
        
        for (int i = 0; i < menuItems.length; i++) {
            JButton button = createSidebarButton(icons[i] + "  " + menuItems[i], menuItems[i]);
            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(5));
            
            if (i == 0) { // Select dashboard by default
                button.setBackground(ACCENT_COLOR);
                selectedButton = button;
            }
        }
        
        // Add flexible space to push content to top
        sidebar.add(Box.createVerticalGlue());
        
        return sidebar;
    }
    
    private JButton createSidebarButton(String text, String panelName) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setForeground(TEXT_COLOR);
        button.setBackground(PANEL_COLOR);
        button.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 50));
        button.setPreferredSize(new Dimension(200, 50));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setToolTipText(text); // Add tooltip for full text on hover
        
        // Create rounded border
        button.setBorder(new RoundedBorder(8));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button != selectedButton) {
                    button.setBackground(HOVER_COLOR.darker().darker());
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (button != selectedButton) {
                    button.setBackground(PANEL_COLOR);
                }
            }
        });
        
        // Add click listener
        button.addActionListener(e -> {
            // Reset previous selected button
            if (selectedButton != null) {
                selectedButton.setBackground(PANEL_COLOR);
            }
            
            // Set new selected button
            button.setBackground(ACCENT_COLOR);
            selectedButton = button;
            
            // Switch panel
            cardLayout.show(mainContentPanel, panelName);
        });
        
        return button;
    }
    
    // Custom rounded border class
    private static class RoundedBorder extends LineBorder {
        private int radius;
        
        public RoundedBorder(int radius) {
            super(BORDER_COLOR, 1);
            this.radius = radius;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(getLineColor());
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new Main().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Error starting application: " + e.getMessage() + 
                    "\n\nMake sure SQLite JDBC driver is available.", 
                    "Startup Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}