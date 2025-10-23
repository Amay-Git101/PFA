package ui;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class PieChartPanel extends JPanel {
    private Map<String, Double> data;
    private String title;
    private static final Color[] COLORS = {
        new Color(0, 200, 151),
        new Color(255, 193, 7),
        new Color(244, 67, 54),
        new Color(33, 150, 243),
        new Color(76, 175, 80),
        new Color(156, 39, 176),
        new Color(255, 87, 34),
        new Color(0, 150, 136)
    };
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color BACKGROUND_COLOR = new Color(42, 42, 42);
    
    public PieChartPanel(String title, Map<String, Double> data) {
        this.title = title;
        this.data = data;
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (data == null || data.isEmpty()) {
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            g2d.drawString("No data available", getWidth() / 2 - 50, getHeight() / 2);
            return;
        }
        
        // Draw title
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(title, (getWidth() - fm.stringWidth(title)) / 2, 25);
        
        // Calculate total
        double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total == 0) {
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            g2d.drawString("No data to display", getWidth() / 2 - 60, getHeight() / 2);
            return;
        }
        
        // Draw pie chart
        int centerX = getWidth() / 2 - 80;
        int centerY = getHeight() / 2;
        int radius = 100;
        
        double currentAngle = -90;
        int colorIndex = 0;
        
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            double percentage = (entry.getValue() / total) * 100;
            double angle = (percentage / 100.0) * 360;
            
            g2d.setColor(COLORS[colorIndex % COLORS.length]);
            g2d.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 
                        (int) currentAngle, (int) angle);
            
            // Draw border
            g2d.setColor(new Color(30, 30, 30));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2,
                       (int) currentAngle, (int) angle);
            
            currentAngle += angle;
            colorIndex++;
        }
        
        // Draw legend
        int legendX = centerX + radius + 40;
        int legendY = centerY - (data.size() * 20) / 2;
        colorIndex = 0;
        
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            double percentage = (entry.getValue() / total) * 100;
            
            // Draw color box
            g2d.setColor(COLORS[colorIndex % COLORS.length]);
            g2d.fillRect(legendX, legendY + colorIndex * 25, 12, 12);
            
            // Draw text
            g2d.setColor(TEXT_COLOR);
            String label = String.format("%s (%.1f%%, $%.2f)", entry.getKey(), percentage, entry.getValue());
            g2d.drawString(label, legendX + 18, legendY + colorIndex * 25 + 10);
            
            colorIndex++;
        }
    }
}
