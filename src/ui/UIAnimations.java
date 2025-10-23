package ui;

import javax.swing.*;
import java.awt.*;

public class UIAnimations {
    
    /**
     * Smoothly update a component's opacity
     */
    public static void fadeIn(JComponent component, int duration) {
        Timer timer = new Timer(30, null);
        final int[] elapsed = {0};
        
        timer.addActionListener(e -> {
            elapsed[0] += 30;
            float progress = Math.min(1.0f, (float) elapsed[0] / duration);
            component.setOpaque(true);
            component.repaint();
            
            if (elapsed[0] >= duration) {
                ((Timer) e.getSource()).stop();
            }
        });
        
        timer.start();
    }
    
    /**
     * Add a subtle highlight effect on table updates
     */
    public static void highlightTableRow(JTable table, int row, int duration) {
        if (row < 0 || row >= table.getRowCount()) return;
        
        // Store original color
        Color originalColor = table.getSelectionBackground();
        
        Timer timer = new Timer(50, null);
        final int[] elapsed = {0};
        final Color highlightColor = new Color(0, 200, 151, 100);
        
        timer.addActionListener(e -> {
            elapsed[0] += 50;
            
            if (elapsed[0] >= duration) {
                ((Timer) e.getSource()).stop();
                table.setSelectionBackground(originalColor);
                table.repaint();
            }
        });
        
        table.setSelectionBackground(highlightColor);
        timer.start();
    }
    
    /**
     * Animate a button press effect
     */
    public static void pressButtonEffect(AbstractButton button) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                button.setMargin(new Insets(3, 5, 3, 5));
                button.repaint();
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                button.setMargin(new Insets(1, 3, 1, 3));
                button.repaint();
            }
        });
    }
    
    /**
     * Smooth scroll animation for table
     */
    public static void smoothScrollToRow(JTable table, int row) {
        SwingUtilities.invokeLater(() -> {
            if (row < 0 || row >= table.getRowCount()) return;
            
            Rectangle rect = table.getCellRect(row, 0, true);
            JViewport viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, table);
            
            if (viewport != null) {
                int currentY = viewport.getViewPosition().y;
                int targetY = Math.max(0, rect.y - viewport.getHeight() / 2 + rect.height / 2);
                
                animateScroll(viewport, currentY, targetY, 300);
            }
        });
    }
    
    /**
     * Animate viewport scroll
     */
    private static void animateScroll(JViewport viewport, int startY, int endY, int duration) {
        Timer timer = new Timer(30, null);
        final long[] startTime = {System.currentTimeMillis()};
        
        timer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime[0];
            float progress = Math.min(1.0f, (float) elapsed / duration);
            
            // Ease-out cubic for smooth deceleration
            progress = 1.0f - (float) Math.pow(1.0f - progress, 3);
            
            int currentY = startY + (int) ((endY - startY) * progress);
            viewport.setViewPosition(new Point(0, currentY));
            
            if (elapsed >= duration) {
                ((Timer) e.getSource()).stop();
                viewport.setViewPosition(new Point(0, endY));
            }
        });
        
        timer.start();
    }
    
    /**
     * Add a glow effect on component focus
     */
    public static void addFocusGlow(JComponent component, Color glowColor) {
        component.addFocusListener(new java.awt.event.FocusAdapter() {
            Color originalBorder;
            
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                originalBorder = component.getBorder() != null ? 
                    ((javax.swing.border.LineBorder) component.getBorder()).getLineColor() : null;
                component.setBorder(BorderFactory.createLineBorder(glowColor, 2));
                component.repaint();
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                component.setBorder(originalBorder != null ? 
                    BorderFactory.createLineBorder(originalBorder, 1) : null);
                component.repaint();
            }
        });
    }
    
    /**
     * Pulse animation for important labels
     */
    public static void pulseLabel(JLabel label, int pulses) {
        Timer timer = new Timer(500, null);
        final int[] count = {0};
        final Color originalColor = label.getForeground();
        final Color pulseColor = new Color(0, 200, 151);
        
        timer.addActionListener(e -> {
            if (count[0] % 2 == 0) {
                label.setForeground(pulseColor);
            } else {
                label.setForeground(originalColor);
            }
            
            count[0]++;
            if (count[0] >= pulses * 2) {
                ((Timer) e.getSource()).stop();
                label.setForeground(originalColor);
            }
        });
        
        timer.start();
    }
}
