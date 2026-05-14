package com.example.roboflowredmine.service;

import com.example.roboflowredmine.dto.InferenceResultDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class RedmineService {

    @Value("${redmine.url}")
    private String redmineUrl;

    @Value("${redmine.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RedmineService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

 
    public Integer createIssue(Integer projectId, String subject, String description) throws Exception {
        String url = redmineUrl + "/issues.json";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Redmine-API-Key", apiKey);

        Map<String, Object> issue = new HashMap<>();
        issue.put("project_id", projectId);
        issue.put("subject", subject);
        issue.put("description", description);
        issue.put("tracker_id", 1);   // 1 = Bug (prilagodi svom Redmine-u)

        Map<String, Object> body = new HashMap<>();
        body.put("issue", issue);

        String jsonBody = objectMapper.writeValueAsString(body);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Redmine API error: " + response.getStatusCode() + " - " + response.getBody());
        }

        JsonNode root = objectMapper.readTree(response.getBody());
        return root.path("issue").path("id").asInt();
    }

    
    public Integer createFromInference(InferenceResultDTO dto, String modelId, Integer projectId) throws Exception {
        StringBuilder subject = new StringBuilder("Inference: ");
        subject.append(modelId).append(" | ").append(dto.count).append(" objects detected");

        StringBuilder description = new StringBuilder();
        description.append("**Count objects:** ").append(dto.count).append("\n\n");
        description.append("**Predictions:**\n");
        for (var pred : dto.predictions) {
            description.append("- ").append(pred.className)
                    .append(" (conf: ").append(String.format("%.2f", pred.confidence)).append(")")
                    .append(" at (").append(pred.x).append(",").append(pred.y).append(")")
                    .append(" size ").append(pred.width).append("x").append(pred.height)
                    .append("\n");
        }

        return createIssue(projectId, subject.toString(), description.toString());
    }
}
