package com.example.roboflowredmine.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class RoboflowService {

    @Value("${roboflow.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RoboflowService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

   
    public JsonNode runWorkflow(String imageUrl) throws Exception {
        String url = "https://serverless.roboflow.com/ognjens-workspace-rujhp/workflows/detect-count-and-visualize-2";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("api_key", apiKey);
        
        Map<String, Object> inputs = new HashMap<>();
        Map<String, Object> imageInput = new HashMap<>();
        imageInput.put("type", "url");
        imageInput.put("value", imageUrl);
        inputs.put("image", imageInput);
        requestBody.put("inputs", inputs);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Roboflow error: " + response.getStatusCode() + " - " + response.getBody());
        }
        
        return objectMapper.readTree(response.getBody());
    }
}