package service;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * GeminiService - Integration with Google's Gemini AI API
 * WHY: Provides intelligent financial advice using Gemini 1.5 Flash model
 */
public class GeminiService {
    private static final String GEMINI_API_BASE = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    private static final String CONFIG_FILE = "config.properties";
    
    private static final String SYSTEM_PROMPT = """
        You are FinSight AI — a personal finance advisor integrated within the FinSight desktop app.
        You can access the user's financial data including expenses, budgets, investments, and reports.
        Your role:
        - Answer financial questions naturally using the given data.
        - Explain if the user is over or under budget.
        - Suggest tips, savings plans, and summarize spending trends.
        - Always respond in a friendly, concise tone using the user's preferred currency.
        If data is missing, mention it clearly instead of making assumptions.
        """;
    
    private String apiKey;
    private boolean enabled;
    
    public GeminiService() {
        loadConfig();
    }
    
    /**
     * Load configuration from config.properties
     */
    private void loadConfig() {
        Properties props = new Properties();
        File configFile = new File(CONFIG_FILE);
        
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
                this.apiKey = props.getProperty("gemini_api_key", "");
                this.enabled = !this.apiKey.isEmpty();
            } catch (IOException e) {
                System.err.println("Error loading config: " + e.getMessage());
                this.enabled = false;
            }
        } else {
            this.enabled = false;
        }
    }
    
    /**
     * Reload configuration from file
     * WHY: Allows reloading config after user updates settings
     */
    public void reloadConfig() {
        loadConfig();
    }
    
    /**
     * Check if Gemini service is configured and ready
     */
    public boolean isEnabled() {
        return enabled && apiKey != null && !apiKey.isEmpty();
    }
    
    /**
     * Get configuration error message if service is not enabled
     */
    public String getConfigMessage() {
        if (!enabled || apiKey == null || apiKey.isEmpty()) {
            return "Gemini AI is not configured. Please set your API key in Settings.";
        }
        return null;
    }
    
    /**
     * Ask Gemini AI with user's financial context
     * @param userQuestion The user's question
     * @param financialContext JSON object containing user's financial data
     * @return AI response text
     */
    public String askGemini(String userQuestion, JSONObject financialContext) throws IOException {
        if (!isEnabled()) {
            return getConfigMessage();
        }
        
        // Build the full prompt with context
        String fullPrompt = buildPromptWithContext(userQuestion, financialContext);
        
        // Create request payload for Gemini API
        JSONObject requestBody = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        
        // Add system prompt
        JSONObject systemPart = new JSONObject();
        systemPart.put("text", SYSTEM_PROMPT);
        parts.put(systemPart);
        
        // Add user question with context
        JSONObject userPart = new JSONObject();
        userPart.put("text", fullPrompt);
        parts.put(userPart);
        
        content.put("parts", parts);
        contents.put(content);
        requestBody.put("contents", contents);
        
        // Add generation config
        JSONObject generationConfig = new JSONObject();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 800);
        requestBody.put("generationConfig", generationConfig);
        
        // Make API request
        String response = callGeminiAPI(requestBody);
        
        // Parse and return response
        return parseGeminiResponse(response);
    }
    
    /**
     * Build prompt with financial context
     */
    private String buildPromptWithContext(String question, JSONObject context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("User's Financial Data Summary:\n");
        prompt.append(context.toString(2)); // Pretty print with indent
        prompt.append("\n\nUser Question: ").append(question);
        prompt.append("\n\nPlease provide a helpful, natural response using the financial data above.");
        return prompt.toString();
    }
    
    /**
     * Call Gemini API endpoint
     */
    private String callGeminiAPI(JSONObject payload) throws IOException {
        String urlString = GEMINI_API_BASE + "?key=" + apiKey;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);
        
        // Send request
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        // Read response
        int responseCode = conn.getResponseCode();
        
        if (responseCode == 200) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }
        } else {
            // Read error response
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder error = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    error.append(line);
                }
                throw new IOException("Gemini API error (code " + responseCode + "): " + error.toString());
            }
        }
    }
    
    /**
     * Parse Gemini API response
     */
    private String parseGeminiResponse(String jsonResponse) {
        try {
            JSONObject root = new JSONObject(jsonResponse);
            
            // Navigate through Gemini's response structure
            if (root.has("candidates")) {
                JSONArray candidates = root.getJSONArray("candidates");
                if (candidates.length() > 0) {
                    JSONObject candidate = candidates.getJSONObject(0);
                    if (candidate.has("content")) {
                        JSONObject content = candidate.getJSONObject("content");
                        if (content.has("parts")) {
                            JSONArray parts = content.getJSONArray("parts");
                            if (parts.length() > 0) {
                                JSONObject part = parts.getJSONObject(0);
                                if (part.has("text")) {
                                    return part.getString("text");
                                }
                            }
                        }
                    }
                }
            }
            
            // Fallback if structure is different
            return "Unable to parse AI response. Raw response: " + jsonResponse;
            
        } catch (Exception e) {
            return "Error parsing AI response: " + e.getMessage();
        }
    }
    
    /**
     * Test the Gemini API connection
     */
    public boolean testConnection() {
        if (!isEnabled()) {
            return false;
        }
        
        try {
            JSONObject testContext = new JSONObject();
            testContext.put("test", "connection");
            String response = askGemini("Say 'Connection successful' if you can read this.", testContext);
            return response != null && !response.contains("Error");
        } catch (Exception e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Save API key to config file
     */
    public static boolean saveApiKey(String apiKey) {
        Properties props = new Properties();
        File configFile = new File(CONFIG_FILE);
        
        // Load existing properties if file exists
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
            } catch (IOException e) {
                System.err.println("Error loading existing config: " + e.getMessage());
            }
        }
        
        // Update API key
        props.setProperty("gemini_api_key", apiKey);
        props.setProperty("llm_provider", "gemini");
        
        // Save to file
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            props.store(fos, "FinSight AI Configuration");
            return true;
        } catch (IOException e) {
            System.err.println("Error saving config: " + e.getMessage());
            return false;
        }
    }
}
