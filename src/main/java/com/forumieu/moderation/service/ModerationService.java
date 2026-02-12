package com.forumieu.moderation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.forumieu.moderation.model.ModerationRequest;
import com.forumieu.moderation.model.ModerationResponse;
import com.forumieu.moderation.model.ModerationStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class ModerationService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${moderation.confidence-threshold}")
    private double confidenceThreshold;

    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong flaggedCount = new AtomicLong(0);
    private final AtomicLong safeCount = new AtomicLong(0);

    private static final String MODERATION_PROMPT = """
            You are a content moderation system for a university forum platform.
            Analyze the following user-generated text and determine if it is safe or should be flagged.

            Categories:
            - "clean": The content is appropriate
            - "spam": The content is spam or advertising
            - "profanity": The content contains offensive language or hate speech
            - "harassment": The content targets or bullies other users

            Respond ONLY in this exact JSON format, nothing else:
            {"safe": true, "category": "clean", "confidence": 0.95, "reason": "brief explanation"}

            Text to analyze: """;

    public ModerationResponse moderateText(ModerationRequest request) {
        totalRequests.incrementAndGet();

        try {
            String fullPrompt = MODERATION_PROMPT + request.getText();

            // Build request body using ObjectMapper
            ObjectNode partNode = objectMapper.createObjectNode();
            partNode.put("text", fullPrompt);

            ArrayNode partsArray = objectMapper.createArrayNode();
            partsArray.add(partNode);

            ObjectNode contentNode = objectMapper.createObjectNode();
            contentNode.set("parts", partsArray);

            ArrayNode contentsArray = objectMapper.createArrayNode();
            contentsArray.add(contentNode);

            ObjectNode generationConfig = objectMapper.createObjectNode();
            generationConfig.put("temperature", 0.1);
            generationConfig.put("maxOutputTokens", 1024);
            generationConfig.put("responseMimeType", "application/json");

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.set("contents", contentsArray);
            requestBody.set("generationConfig", generationConfig);

            String body = objectMapper.writeValueAsString(requestBody);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Gemini API returned status {}", response.statusCode());
                throw new RuntimeException("Gemini API returned status " + response.statusCode());
            }

            return parseGeminiResponse(response.body(), request.getPostId());

        } catch (Exception e) {
            log.error("Moderation API call failed: {}", e.getMessage());
            return ModerationResponse.builder()
                    .safe(true)
                    .category("error")
                    .confidence(0.0)
                    .reason("Moderation service temporarily unavailable, defaulting to safe")
                    .postId(request.getPostId())
                    .build();
        }
    }

    private ModerationResponse parseGeminiResponse(String responseBody, String postId) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String text = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            // Clean markdown code block if present
            text = text.replace("```json", "").replace("```", "").trim();

            JsonNode result = objectMapper.readTree(text);

            boolean safe = result.path("safe").asBoolean(true);
            String category = result.path("category").asText("clean");
            double confidence = result.path("confidence").asDouble(0.5);
            String reason = result.path("reason").asText("");

            if (!safe && confidence >= confidenceThreshold) {
                flaggedCount.incrementAndGet();
            } else {
                if (safe) safeCount.incrementAndGet();
                else safeCount.incrementAndGet();
            }

            return ModerationResponse.builder()
                    .safe(safe)
                    .category(category)
                    .confidence(confidence)
                    .reason(reason)
                    .postId(postId)
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", e.getMessage());
            safeCount.incrementAndGet();
            return ModerationResponse.builder()
                    .safe(true)
                    .category("parse_error")
                    .confidence(0.0)
                    .reason("Could not parse AI response, defaulting to safe")
                    .postId(postId)
                    .build();
        }
    }

    public ModerationStats getStats() {
        long total = totalRequests.get();
        long flagged = flaggedCount.get();
        long safe = safeCount.get();
        double percentage = total > 0 ? (double) flagged / total * 100 : 0.0;

        return ModerationStats.builder()
                .totalRequests(total)
                .flaggedCount(flagged)
                .safeCount(safe)
                .flaggedPercentage(Math.round(percentage * 100.0) / 100.0)
                .build();
    }
}
