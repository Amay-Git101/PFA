package service;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * OpenRouterService - Integration with OpenRouter API
 * Allows using various LLMs including Gemini through OpenRouter
 */
public class OpenRouterService {
    private static final String OPENROUTER_API_URL = "https://openrouter.ai/api/v1/chat/completions";
    
    private static final String SYSTEM_PROMPT = """
        You are FinSight AI — a personal finance advisor integrated within the FinSight desktop app.
        You can access the user's financial data including expenses, budgets, and reports.
        Your role:
        - Answer financial questions naturally using the given data.
        - Explain if the user is over or under budget.
        - Suggest tips, savings plans, and summarize spending trends.
        - Always respond in a friendly, concise tone using the user's preferred currency.
        If data is missing, mention it clearly instead of making assumptions.
        """;
    
    private String apiKey;
    private String model;
    
    public OpenRouterService(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model != null ? model : "google/gemini-2.0-flash-exp:free";
    }
    
    public boolean isEnabled() {
        return apiKey != null && !apiKey.isEmpty();
    }
    
    public String ask(String userQuestion, JSONObject financialContext) throws IOException {
        if (!isEnabled()) {
            return "OpenRouter API key not configured.";
        }
        
        // Build request
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);
        
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
            .put("role", "system")
            .put("content", SYSTEM_PROMPT));
        
        String userPrompt = "User's Financial Data Summary:\n" +
            financialContext.toString(2) +
            "\n\nUser Question: " + userQuestion +
            "\n\nPlease provide a helpful, natural response using the financial data above.";
        
        messages.put(new JSONObject()
            .put("role", "user")
            .put("content", userPrompt));
        
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 800);
        
        // Call API
        String response = callAPI(requestBody);
        return parseResponse(response);
    }
    
    private String callAPI(JSONObject payload) throws IOException {
        URL url = new URL(OPENROUTER_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("HTTP-Referer", "http://localhost");
        conn.setRequestProperty("X-Title", "FinSight Personal Finance Advisor");
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
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder error = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    error.append(line);
                }
                throw new IOException("OpenRouter API error (code " + responseCode + "): " + error.toString());
            }
        }
    }
    
    private String parseResponse(String jsonResponse) {
        try {
            JSONObject root = new JSONObject(jsonResponse);
            
            if (root.has("choices")) {
                JSONArray choices = root.getJSONArray("choices");
                if (choices.length() > 0) {
                    JSONObject choice = choices.getJSONObject(0);
                    if (choice.has("message")) {
                        JSONObject message = choice.getJSONObject("message");
                        if (message.has("content")) {
                            return message.getString("content");
                        }
                    }
                }
            }
            
            return "Unable to parse AI response. Raw: " + jsonResponse;
            
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }
}
