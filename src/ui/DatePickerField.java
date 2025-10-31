package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class DatePickerField extends JPanel {
    private JTextField dateField;
    private JButton calendarButton;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Theme colors
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 30);
    private static final Color PANEL_COLOR = new Color(42, 42, 42);
    private static final Color ACCENT_COLOR = new Color(0, 200, 151);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(60, 60, 60);
    private static final Color HOVER_COLOR = new Color(0, 230, 168);
    
    public DatePickerField() {
        setLayout(new BorderLayout(5, 0));
        setBackground(BACKGROUND_COLOR);
        
        // Text field for date input
        dateField = new JTextField(10);
        dateField.setBackground(PANEL_COLOR);
        dateField.setForeground(TEXT_COLOR);
        dateField.setCaretColor(TEXT_COLOR);
        dateField.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        dateField.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        // Calendar button
        calendarButton = new JButton("📅");
        calendarButton.setBackground(ACCENT_COLOR);
        calendarButton.setForeground(TEXT_COLOR);
        calendarButton.setFocusPainted(false);
        calendarButton.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        calendarButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        calendarButton.setToolTipText("Select date from calendar");
        
        calendarButton.addActionListener(e -> showCalendar());
        
        add(dateField, BorderLayout.CENTER);
        add(calendarButton, BorderLayout.EAST);
    }
    
    public String getText() {
        return dateField.getText();
    }
    
    public void setText(String text) {
        dateField.setText(text);
    }
    
    private void showCalendar() {
        JDialog calendarDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Date", true);
        calendarDialog.setLayout(new BorderLayout());
        calendarDialog.getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Parse current date or use today
        LocalDate selectedDate;
        try {
            String currentText = dateField.getText().trim();
            selectedDate = currentText.isEmpty() ? LocalDate.now() : LocalDate.parse(currentText, DATE_FORMAT);
        } catch (Exception e) {
            selectedDate = LocalDate.now();
        }
        
        CalendarPanel calendarPanel = new CalendarPanel(selectedDate, date -> {
            dateField.setText(date.format(DATE_FORMAT));
            calendarDialog.dispose();
        });
        
        calendarDialog.add(calendarPanel, BorderLayout.CENTER);
        calendarDialog.pack();
        calendarDialog.setLocationRelativeTo(this);
        calendarDialog.setVisible(true);
    }
    
    // Inner class for calendar panel
    private class CalendarPanel extends JPanel {
        private LocalDate currentMonth;
        private JLabel monthYearLabel;
        private JPanel daysPanel;
        private DateSelectListener listener;
        
        public CalendarPanel(LocalDate initialDate, DateSelectListener listener) {
            this.currentMonth = YearMonth.from(initialDate).atDay(1);
            this.listener = listener;
            
            setLayout(new BorderLayout(10, 10));
            setBackground(BACKGROUND_COLOR);
            setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            
            // Header with month/year and navigation
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(BACKGROUND_COLOR);
            
            JButton prevButton = createNavButton("◀");
            prevButton.addActionListener(e -> {
                currentMonth = currentMonth.minusMonths(1);
                updateCalendar();
            });
            
            JButton nextButton = createNavButton("▶");
            nextButton.addActionListener(e -> {
                currentMonth = currentMonth.plusMonths(1);
                updateCalendar();
            });
            
            monthYearLabel = new JLabel("", SwingConstants.CENTER);
            monthYearLabel.setForeground(ACCENT_COLOR);
            monthYearLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            
            headerPanel.add(prevButton, BorderLayout.WEST);
            headerPanel.add(monthYearLabel, BorderLayout.CENTER);
            headerPanel.add(nextButton, BorderLayout.EAST);
            
            // Days of week header
            JPanel weekHeaderPanel = new JPanel(new GridLayout(1, 7, 5, 5));
            weekHeaderPanel.setBackground(BACKGROUND_COLOR);
            String[] weekDays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
            for (String day : weekDays) {
                JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
                dayLabel.setForeground(ACCENT_COLOR);
                dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
                weekHeaderPanel.add(dayLabel);
            }
            
            // Days panel
            daysPanel = new JPanel(new GridLayout(6, 7, 5, 5));
            daysPanel.setBackground(BACKGROUND_COLOR);
            
            add(headerPanel, BorderLayout.NORTH);
            add(weekHeaderPanel, BorderLayout.CENTER);
            add(daysPanel, BorderLayout.SOUTH);
            
            // Today button
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            bottomPanel.setBackground(BACKGROUND_COLOR);
            JButton todayButton = new JButton("Today");
            todayButton.setBackground(ACCENT_COLOR);
            todayButton.setForeground(TEXT_COLOR);
            todayButton.setFocusPainted(false);
            todayButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
            todayButton.addActionListener(e -> listener.onDateSelected(LocalDate.now()));
            bottomPanel.add(todayButton);
            add(bottomPanel, BorderLayout.SOUTH);
            
            updateCalendar();
        }
        
        private JButton createNavButton(String text) {
            JButton button = new JButton(text);
            button.setBackground(ACCENT_COLOR);
            button.setForeground(TEXT_COLOR);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return button;
        }
        
        private void updateCalendar() {
            monthYearLabel.setText(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
            daysPanel.removeAll();
            
            // Get first day of month and number of days
            YearMonth yearMonth = YearMonth.from(currentMonth);
            int daysInMonth = yearMonth.lengthOfMonth();
            int firstDayOfWeek = currentMonth.getDayOfWeek().getValue() % 7; // Sunday = 0
            
            LocalDate today = LocalDate.now();
            
            // Add empty cells for days before month starts
            for (int i = 0; i < firstDayOfWeek; i++) {
                JLabel emptyLabel = new JLabel("");
                daysPanel.add(emptyLabel);
            }
            
            // Add day buttons
            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = currentMonth.withDayOfMonth(day);
                JButton dayButton = new JButton(String.valueOf(day));
                dayButton.setBackground(PANEL_COLOR);
                dayButton.setForeground(TEXT_COLOR);
                dayButton.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
                dayButton.setFocusPainted(false);
                dayButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
                dayButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                
                // Highlight today
                if (date.equals(today)) {
                    dayButton.setBackground(ACCENT_COLOR);
                    dayButton.setForeground(BACKGROUND_COLOR);
                    dayButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
                }
                
                dayButton.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (!date.equals(today)) {
                            dayButton.setBackground(HOVER_COLOR.darker());
                        }
                    }
                    
                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (!date.equals(today)) {
                            dayButton.setBackground(PANEL_COLOR);
                        }
                    }
                });
                
                dayButton.addActionListener(e -> listener.onDateSelected(date));
                
                daysPanel.add(dayButton);
            }
            
            daysPanel.revalidate();
            daysPanel.repaint();
        }
    }
    
    @FunctionalInterface
    private interface DateSelectListener {
        void onDateSelected(LocalDate date);
    }
}
