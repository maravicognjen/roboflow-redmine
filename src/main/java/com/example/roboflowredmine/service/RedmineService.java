package com.example.roboflowredmine.service;

import com.example.roboflowredmine.dto.InferenceResultDTO;
import com.example.roboflowredmine.dto.PredictionDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

        String jsonBody = String.format(
            "{\"issue\":{\"project_id\":%d,\"subject\":\"%s\",\"description\":\"%s\"}}",
            projectId, subject, description.replace("\"", "\\\"")
        );

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Redmine API error: " + response.getStatusCode() + " - " + response.getBody());
        }

        JsonNode root = objectMapper.readTree(response.getBody());
        return root.path("issue").path("id").asInt();
    }
    public Integer createFromInference(
            InferenceResultDTO result,
            String modelName,
            Integer projectId
    ) throws Exception {

        String title = "Detection: " +
                result.predictions.stream()
                        .map(p -> p.clazz)
                        .distinct()
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("unknown");

        StringBuilder desc = new StringBuilder();

        desc.append("Model: ").append(modelName).append("\n");
        desc.append("Total detections: ")
            .append(result.predictions.size())
            .append("\n\n");

        for (PredictionDTO p : result.predictions) {
            desc.append(p.clazz)
                    .append(" - ")
                    .append(p.confidence)
                    .append("\n");
        }

        return createIssue(projectId, title, desc.toString());
    }
}
