package com.example.roboflowredmine.service;

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
}
