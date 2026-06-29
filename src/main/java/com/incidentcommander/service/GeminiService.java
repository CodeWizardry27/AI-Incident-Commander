package com.incidentcommander.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    private final WebClient geminiWebClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String apiKey;

    /**
     * Send a conversation to Gemini and get a response.
     *
     * @param systemPrompt         The system instruction for the AI
     * @param conversationHistory  List of maps with "role" (user/model) and "text" keys
     * @return The AI's text response
     */
    public String chat(String systemPrompt, List<Map<String, String>> conversationHistory) {
        try {
            ObjectNode requestBody = buildRequestBody(systemPrompt, conversationHistory);

            log.debug("Sending request to Gemini API...");
            String response = geminiWebClient.post()
                    .uri("?key=" + apiKey)
                    .bodyValue(requestBody.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            String result = extractTextFromResponse(response);
            log.debug("Gemini response received ({} chars)", result.length());
            return result;
        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage(), e);
            return "ERROR: Failed to get AI response - " + e.getMessage();
        }
    }

    /**
     * Simple single-message call to Gemini.
     */
    public String ask(String systemPrompt, String userMessage) {
        return chat(systemPrompt, List.of(Map.of("role", "user", "text", userMessage)));
    }

    private ObjectNode buildRequestBody(String systemPrompt, List<Map<String, String>> history) {
        ObjectNode root = objectMapper.createObjectNode();

        // System instruction
        ObjectNode systemInstruction = objectMapper.createObjectNode();
        ArrayNode systemParts = objectMapper.createArrayNode();
        ObjectNode systemPart = objectMapper.createObjectNode();
        systemPart.put("text", systemPrompt);
        systemParts.add(systemPart);
        systemInstruction.set("parts", systemParts);
        root.set("system_instruction", systemInstruction);

        // Conversation contents
        ArrayNode contents = objectMapper.createArrayNode();
        for (Map<String, String> msg : history) {
            ObjectNode content = objectMapper.createObjectNode();
            content.put("role", msg.get("role"));
            ArrayNode parts = objectMapper.createArrayNode();
            ObjectNode part = objectMapper.createObjectNode();
            part.put("text", msg.get("text"));
            parts.add(part);
            content.set("parts", parts);
            contents.add(content);
        }
        root.set("contents", contents);

        // Generation config
        ObjectNode generationConfig = objectMapper.createObjectNode();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 2048);
        root.set("generationConfig", generationConfig);

        return root;
    }

    private String extractTextFromResponse(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            return root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", responseJson);
            return "ERROR: Failed to parse AI response";
        }
    }
}
