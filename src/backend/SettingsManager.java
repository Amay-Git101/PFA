package backend;

import database.AppSettingsDAO;

/**
 * Centralized settings manager for the application.
 * Handles currency and theme preferences.
 */
public class SettingsManager {
    private static AppSettingsDAO settingsDAO = new AppSettingsDAO();
    private static String currentCurrencyCode;
    private static String currentTheme;

    /**
     * Load settings from database on startup
     */
    public static void loadSettings() {
        currentCurrencyCode = settingsDAO.getSetting("currency", "INR");
        currentTheme = settingsDAO.getSetting("theme", "Dark");
        
        System.out.println("Settings loaded: Currency=" + currentCurrencyCode + ", Theme=" + currentTheme);
    }

    // --- Currency Management ---
    
    /**
     * Get the current currency code (e.g., "INR", "USD")
     */
    public static String getCurrencyCode() {
        return currentCurrencyCode;
    }

    /**
     * Get the currency symbol for display (e.g., "₹", "$")
     */
    public static String getCurrencySymbol() {
        switch (currentCurrencyCode) {
            case "USD":
                return "$";
            case "INR":
                return "₹";
            case "EUR":
                return "€";
            case "GBP":
                return "£";
            default:
                return "₹"; // Default to INR
        }
    }

    /**
     * Set the currency and save to database
     */
    public static void setCurrency(String code) {
        currentCurrencyCode = code;
        settingsDAO.setSetting("currency", code);
        System.out.println("Currency updated to: " + code);
    }

    // --- Theme Management ---
    
    /**
     * Get the current theme name
     */
    public static String getTheme() {
        return currentTheme;
    }

    /**
     * Set the theme and save to database
     */
    public static void setTheme(String themeName) {
        currentTheme = themeName;
        settingsDAO.setSetting("theme", themeName);
        System.out.println("Theme updated to: " + themeName);
    }
    
    /**
     * Check if dark theme is active
     */
    public static boolean isDarkTheme() {
        return "Dark".equalsIgnoreCase(currentTheme);
    }
}
