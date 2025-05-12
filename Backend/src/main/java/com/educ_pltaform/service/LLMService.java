package com.educ_pltaform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LLMService {

    @Value("${deepseek.api.url}")
    private String llmApiUrl;

    @Value("${deepseek.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public LLMService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String generateContent(String prompt, String role) {
        try {
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("model", "anthropic/claude-3-sonnet-20240229");
            requestMap.put("messages", List.of(
                    Map.of("role", "system", "content", role),
                    Map.of("role", "user", "content", prompt)
            ));
            requestMap.put("max_tokens", 500);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("HTTP-Referer", "http://localhost:3000");
            headers.set("X-Title", "Educational Platform");
            headers.set("Authorization", "Bearer " + apiKey);

            log.info("Envoi requête à {}", llmApiUrl + "/chat/completions");
            log.info("Requête: {}", objectMapper.writeValueAsString(requestMap));

            HttpEntity<Map<String, Object>> requestEntity =
                    new HttpEntity<>(requestMap, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    llmApiUrl /* + "/chat/completions"*/,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info("Réponse: {}", response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                return jsonResponse.path("choices")
                        .path(0)
                        .path("message")
                        .path("content")
                        .asText();
            }

            throw new LLMServiceException("Réponse API invalide");
        } catch (Exception e) {
            log.error("Erreur: ", e);
            throw new LLMServiceException("Erreur API LLM: " + e.getMessage(), e);
        }
    }
}