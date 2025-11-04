package service;

import org.json.JSONObject;
import service.AIService;
import service.AIService.AIResponse;

/**
 * Unit tests for AIService
 * WHY: Validates AI service integration and response parsing
 */
public class AIServiceTest {
    
    /**
     * Test mock provider response generation
     */
    public static void testMockProviderResponse() {
        System.out.println("Testing Mock Provider...");
        
        AIService aiService = new AIService("mock", "", "");
        
        // Create test context
        JSONObject context = new JSONObject();
        context.put("budgetUsagePercent", 85.0);
        context.put("totalExpenses", 2500.00);
        context.put("topExpenseCategory", "Groceries");
        
        try {
            AIResponse response = aiService.askWithContext("Am I over budget?", context);
            
            // Validate response
            assert response != null : "Response should not be null";
            assert response.getReply() != null : "Reply should not be null";
            assert response.getReply().contains("budget") : "Response should mention budget";
            
            System.out.println("✅ Mock provider test passed");
            System.out.println("Response: " + response.getReply());
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test AI service initialization
     */
    public static void testServiceInitialization() {
        System.out.println("\nTesting Service Initialization...");
        
        AIService mockService = new AIService("mock", "", "");
        assert mockService.isEnabled() : "Mock service should be enabled";
        
        AIService disabledService = new AIService("openai", "", "");
        assert !disabledService.isEnabled() : "Service without API key should be disabled";
        
        System.out.println("✅ Service initialization test passed");
    }
    
    /**
     * Test action extraction from responses
     */
    public static void testActionExtraction() {
        System.out.println("\nTesting Action Extraction...");
        
        AIService aiService = new AIService("mock", "", "");
        
        JSONObject context = new JSONObject();
        context.put("budgetUsagePercent", 120.0);
        context.put("totalExpenses", 3000.00);
        context.put("topExpenseCategory", "Dining Out");
        
        try {
            AIResponse response = aiService.askWithContext("Am I over budget?", context);
            
            // Check if actions are present when over budget
            if (response.hasActions()) {
                System.out.println("✅ Actions extracted: " + response.getActions().size());
                for (AIService.AIAction action : response.getActions()) {
                    System.out.println("  - Action type: " + action.getType());
                }
            } else {
                System.out.println("ℹ️  No actions in this response (expected for some queries)");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Run all tests
     */
    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("  AIService Unit Tests");
        System.out.println("=================================\n");
        
        testServiceInitialization();
        testMockProviderResponse();
        testActionExtraction();
        
        System.out.println("\n=================================");
        System.out.println("  All Tests Complete");
        System.out.println("=================================");
    }
}
