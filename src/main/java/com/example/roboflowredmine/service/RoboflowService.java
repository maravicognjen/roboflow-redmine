package com.example.roboflowredmine.service;

import com.example.roboflowredmine.dto.InferenceResultDTO;
import com.example.roboflowredmine.dto.PredictionDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class RoboflowService {

    @Value("${roboflow.api.key}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    
    public String fileToBase64(MultipartFile file) throws Exception {
        return Base64.getEncoder().encodeToString(file.getBytes());
    }

    
    public JsonNode runWorkflow(String base64Image) throws Exception {
        String url = "https://serverless.roboflow.com/infer/workflows/ognjens-workspace-rujhp/detect-count-and-visualize-3";
        
        
        String jsonBody = String.format("""
            {
              "api_key": "%s",
              "inputs": {
                "image": {
                  "type": "base64",
                  "value": "%s"
                }
              }
            }
            """, apiKey, base64Image);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("Roboflow API error: " + response.statusCode() + " - " + response.body());
        }
        
        return objectMapper.readTree(response.body());
    }
    public InferenceResultDTO parse(JsonNode raw) {

        InferenceResultDTO dto = new InferenceResultDTO();

        JsonNode outputs = raw.path("outputs");

        if (outputs.isArray() && outputs.size() > 0) {

            JsonNode out = outputs.get(0);

            // count
            int count = out.path("count_objects").asInt();
            dto.count = count;

            // base64 image
            String image = out.path("output_image").path("value").asText();
            dto.imageBase64 = image;
        }

        return dto;
    }
}