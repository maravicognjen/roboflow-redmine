package com.example.roboflowredmine.service;

import com.example.roboflowredmine.dto.InferenceResultDTO;
import com.example.roboflowredmine.dto.PredictionDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class RoboflowService {

    @Value("${roboflow.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RoboflowService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    
    public String fileToBase64(MultipartFile file) throws Exception {
        return Base64.getEncoder().encodeToString(file.getBytes());
    }

   
    public JsonNode runWorkflow(String base64Image) throws Exception {
        String url = "https://serverless.roboflow.com/infer/workflows/ognjens-workspace-rujhp/detect-count-and-visualize-3";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String jsonBody = String.format(
                "{\"api_key\":\"%s\",\"inputs\":{\"image\":{\"type\":\"base64\",\"value\":\"%s\"}}}",
                apiKey, base64Image
        );

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Roboflow API error: " + response.getStatusCode() + " - " + response.getBody());
        }

        return objectMapper.readTree(response.getBody());
    }

  
    public InferenceResultDTO parse(JsonNode root) {
        InferenceResultDTO dto = new InferenceResultDTO();

       
        if (root.has("outputs") && root.get("outputs").isArray() && root.get("outputs").size() > 0) {
            JsonNode firstOutput = root.get("outputs").get(0);
            if (firstOutput.has("count_objects")) {
                dto.count = firstOutput.get("count_objects").asInt();
            }

           
            JsonNode predictionsNode = firstOutput.path("predictions").path("predictions");
            List<PredictionDTO> predictions = new ArrayList<>();

            for (JsonNode pred : predictionsNode) {
                PredictionDTO p = new PredictionDTO();
                p.className = pred.path("class").asText();
                p.confidence = pred.path("confidence").asDouble();
                p.x = pred.path("x").asDouble();
                p.y = pred.path("y").asDouble();
                p.width = pred.path("width").asDouble();
                p.height = pred.path("height").asDouble();
                predictions.add(p);
            }
            dto.predictions = predictions;

           
            if (firstOutput.has("output_image") && firstOutput.get("output_image").has("value")) {
                dto.imageBase64 = firstOutput.get("output_image").get("value").asText();
            }
        }
        return dto;
    }
}