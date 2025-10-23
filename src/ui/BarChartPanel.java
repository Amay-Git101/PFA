package ui;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class BarChartPanel extends JPanel {
    private Map<String, Double> data;
    private String title;
    private String yAxisLabel;
    private static final Color BAR_COLOR = new Color(0, 200, 151);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color BACKGROUND_COLOR = new Color(42, 42, 42);
    private static final Color GRID_COLOR = new Color(60, 60, 60);
    
    public BarChartPanel(String title, Map<String, Double> data, String yAxisLabel) {
        this.title = title;
        this.data = data;
        this.yAxisLabel = yAxisLabel;
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createLineBorder(GRID_COLOR));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (data == null || data.isEmpty()) {
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            g2d.drawString("No data available", getWidth() / 2 - 60, getHeight() / 2);
            return;
        }
        
        int padding = 60;
        int chartWidth = getWidth() - (padding * 2) - 20;
        int chartHeight = getHeight() - (padding * 2) - 30;
        
        // Draw title
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(title, (getWidth() - fm.stringWidth(title)) / 2, 25);
        
        // Find max value
        double maxValue = data.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        if (maxValue == 0) maxValue = 1.0;
        
        // Draw axes
        g2d.setColor(TEXT_COLOR);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(padding, padding, padding, padding + chartHeight);
        g2d.drawLine(padding, padding + chartHeight, padding + chartWidth, padding + chartHeight);
        
        // Draw Y-axis label
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2d.drawString(yAxisLabel, 10, padding - 10);
        
        // Draw grid lines and Y-axis labels
        g2d.setColor(GRID_COLOR);
        g2d.setStroke(new BasicStroke(1));
        for (int i = 0; i <= 5; i++) {
            int y = padding + chartHeight - (chartHeight / 5) * i;
            double value = (maxValue / 5) * i;
            
            // Grid line
            g2d.drawLine(padding, y, padding + chartWidth, y);
            
            // Y-axis label
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            String label = String.format("$%.0f", value);
            fm = g2d.getFontMetrics();
            g2d.drawString(label, padding - fm.stringWidth(label) - 10, y + 4);
            g2d.setColor(GRID_COLOR);
        }
        
        // Draw bars
        int barCount = data.size();
        double barWidth = (double) chartWidth / barCount;
        int index = 0;
        
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            double barHeight = (entry.getValue() / maxValue) * chartHeight;
            int x = (int) (padding + index * barWidth + barWidth / 4);
            int y = (int) (padding + chartHeight - barHeight);
            int width = (int) (barWidth / 2);
            
            // Draw bar
            g2d.setColor(BAR_COLOR);
            g2d.fillRect(x, y, width, (int) barHeight);
            
            // Draw border
            g2d.setColor(TEXT_COLOR);
            g2d.setStroke(new BasicStroke(1));
            g2d.drawRect(x, y, width, (int) barHeight);
            
            // Draw label
            g2d.setColor(TEXT_COLOR);
            int labelX = (int) (padding + index * barWidth + barWidth / 2);
            int labelY = padding + chartHeight + 20;
            fm = g2d.getFontMetrics();
            g2d.drawString(entry.getKey(), labelX - fm.stringWidth(entry.getKey()) / 2, labelY);
            
            // Draw value on bar
            String valueStr = String.format("$%.0f", entry.getValue());
            g2d.drawString(valueStr, x + width / 2 - fm.stringWidth(valueStr) / 2, y - 5);
            
            index++;
        }
    }
}
