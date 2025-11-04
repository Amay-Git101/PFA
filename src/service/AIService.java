package service;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * AI Service for LLM integration.
 * Supports pluggable LLM providers (OpenAI, local models, etc.)
 * WHY: Provides intelligent financial advice based on user data
 */
public class AIService {
    private final String apiUrl;
    private final String apiKey;
    private final String provider;
    private boolean enabled;
    
    // System prompt that instructs the LLM on its role
    private static final String SYSTEM_PROMPT = """
        You are FinSight AI — a secure, privacy-aware financial advisor embedded in a desktop application.
        You will be given a JSON summary of the user's financial data and an explicit user question.
        
        Always:
        1) Use the JSON to compute facts (budget %, top category, totals).
        2) Answer concisely (3–6 sentences) and clearly.
        3) If the user requests an action (export, reset, set budget rule), produce a JSON "action" object with type and parameters.
        4) When producing actions, DO NOT execute them — return them to the app as JSON only.
        5) Avoid hallucinations; if data is insufficient, say so and ask for clarification.
        
        Response format must be JSON:
        {
          "reply": "Your natural language response here",
          "actions": [
            { "type": "action_type", "param1": "value1", "param2": "value2" }
          ],
          "explainers": ["Additional context or calculations"]
        }
        """;
    
    public AIService(String provider, String apiUrl, String apiKey) {
        this.provider = provider != null ? provider : "mock";
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.enabled = apiKey != null && !apiKey.isEmpty();
    }
    
    /**
     * Check if AI service is properly configured and enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Ask AI with user financial data context
     * WHY: Provides data-aware responses instead of generic advice
     */
    public AIResponse askWithContext(String userPrompt, JSONObject dataContext) throws IOException {
        if (!enabled) {
            return createDisabledResponse();
        }
        
        if ("mock".equals(provider)) {
            return generateMockResponse(userPrompt, dataContext);
        }
        
        // Build request payload for LLM API
        JSONObject requestPayload = new JSONObject();
        requestPayload.put("model", getModelName());
        
        // Construct messages array
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "system").put("content", SYSTEM_PROMPT));
        messages.put(new JSONObject().put("role", "user").put("content", 
            "User's financial data: " + dataContext.toString() + "\n\nUser question: " + userPrompt));
        requestPayload.put("messages", messages);
        requestPayload.put("temperature", 0.7);
        requestPayload.put("max_tokens", 800);
        
        // Make HTTP request
        String response = callLLMAPI(requestPayload);
        
        // Parse response
        return parseAIResponse(response);
    }
    
    /**
     * Simple ask without context (for general questions)
     */
    public String ask(String prompt) {
        try {
            AIResponse response = askWithContext(prompt, new JSONObject());
            return response.getReply();
        } catch (IOException e) {
            return "Error: Unable to connect to AI service. " + e.getMessage();
        }
    }
    
    /**
     * Call the LLM API endpoint
     * WHY: Abstracts HTTP communication with error handling
     */
    private String callLLMAPI(JSONObject payload) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setDoOutput(true);
        
        // Send request
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        // Read response
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("HTTP error code: " + responseCode);
        }
        
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }
    
    /**
     * Parse LLM response into structured AIResponse object
     * WHY: Extracts both text reply and actionable items
     */
    private AIResponse parseAIResponse(String jsonResponse) {
        try {
            JSONObject root = new JSONObject(jsonResponse);
            
            // Extract content from OpenAI-style response
            String content;
            if (root.has("choices")) {
                content = root.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
            } else {
                content = jsonResponse;
            }
            
            // Try to parse content as our expected JSON format
            JSONObject contentJson = new JSONObject(content);
            
            String reply = contentJson.optString("reply", content);
            List<AIAction> actions = new ArrayList<>();
            
            if (contentJson.has("actions")) {
                JSONArray actionsArray = contentJson.getJSONArray("actions");
                for (int i = 0; i < actionsArray.length(); i++) {
                    JSONObject actionObj = actionsArray.getJSONObject(i);
                    actions.add(new AIAction(
                        actionObj.getString("type"),
                        actionObj
                    ));
                }
            }
            
            List<String> explainers = new ArrayList<>();
            if (contentJson.has("explainers")) {
                JSONArray explainersArray = contentJson.getJSONArray("explainers");
                for (int i = 0; i < explainersArray.length(); i++) {
                    explainers.add(explainersArray.getString(i));
                }
            }
            
            return new AIResponse(reply, actions, explainers);
            
        } catch (Exception e) {
            // If parsing fails, return raw response as reply
            return new AIResponse(jsonResponse, new ArrayList<>(), new ArrayList<>());
        }
    }
    
    /**
     * Generate mock responses for testing without actual LLM API
     * WHY: Allows app to work without API key during development/testing
     */
    private AIResponse generateMockResponse(String prompt, JSONObject context) {
        String reply;
        List<AIAction> actions = new ArrayList<>();
        List<String> explainers = new ArrayList<>();
        
        String lowerPrompt = prompt.toLowerCase();
        
        // Extract data from context if available
        double budgetUsage = context.optDouble("budgetUsagePercent", 0);
        double totalExpenses = context.optDouble("totalExpenses", 0);
        String topCategory = context.optString("topExpenseCategory", "Unknown");
        
        if (lowerPrompt.contains("budget") || lowerPrompt.contains("over")) {
            if (budgetUsage > 100) {
                reply = String.format(
                    "You're %.1f%% over your monthly budget. Your largest expense category is %s. " +
                    "I recommend reducing discretionary spending by 15%% and setting up automatic savings.",
                    budgetUsage, topCategory
                );
                actions.add(new AIAction("export_csv", 
                    new JSONObject().put("target", "transactions").put("range", "last_30_days")));
                explainers.add(String.format("Budget usage: %.1f%%", budgetUsage));
            } else {
                reply = String.format(
                    "You're at %.1f%% of your budget - well done! Continue monitoring your %s expenses.",
                    budgetUsage, topCategory
                );
            }
        } else if (lowerPrompt.contains("save") || lowerPrompt.contains("saving")) {
            reply = "Set up automatic transfers to savings. Try the 50/30/20 rule: 50% needs, 30% wants, 20% savings. " +
                   "Start small with $50/week and gradually increase.";
            actions.add(new AIAction("create_rule",
                new JSONObject().put("name", "Auto-save").put("amount", 200).put("frequency", "monthly")));
        } else if (lowerPrompt.contains("invest")) {
            reply = "Start with an emergency fund (3-6 months expenses), then consider low-cost index funds. " +
                   "Diversify and invest regularly regardless of market conditions.";
        } else {
            reply = String.format(
                "Based on your data: Total expenses are $%.2f this month. Your top category is %s. " +
                "Budget usage: %.1f%%. Focus on tracking daily expenses and set category-specific limits.",
                totalExpenses, topCategory, budgetUsage
            );
        }
        
        return new AIResponse(reply, actions, explainers);
    }
    
    private AIResponse createDisabledResponse() {
        return new AIResponse(
            "AI features are disabled. Toggle 'Enable AI Data Upload' in Settings or configure LLM_PROVIDER and LLM_API_KEY.",
            new ArrayList<>(),
            new ArrayList<>()
        );
    }
    
    private String getModelName() {
        if ("openai".equals(provider)) {
            return "gpt-3.5-turbo";
        }
        return "gpt-3.5-turbo"; // default
    }
    
    /**
     * AI Response container class
     */
    public static class AIResponse {
        private final String reply;
        private final List<AIAction> actions;
        private final List<String> explainers;
        
        public AIResponse(String reply, List<AIAction> actions, List<String> explainers) {
            this.reply = reply;
            this.actions = actions;
            this.explainers = explainers;
        }
        
        public String getReply() { return reply; }
        public List<AIAction> getActions() { return actions; }
        public List<String> getExplainers() { return explainers; }
        public boolean hasActions() { return !actions.isEmpty(); }
    }
    
    /**
     * AI Action container class
     */
    public static class AIAction {
        private final String type;
        private final JSONObject parameters;
        
        public AIAction(String type, JSONObject parameters) {
            this.type = type;
            this.parameters = parameters;
        }
        
        public String getType() { return type; }
        public JSONObject getParameters() { return parameters; }
    }
}
