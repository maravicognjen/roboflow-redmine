package com.example.roboflowredmine.test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class TestRoboflow {
    public static void main(String[] args) throws Exception {
        String apiKey = "M30Rc5mxODzOvU7DheKu"; // Zameni sa svojim
        String imagePath = "C:\\Users\\Acim\\Desktop\\img_3.jpg"; // Putanja do neke slike na disku
        
        byte[] imageBytes = Files.readAllBytes(Path.of(imagePath));
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        
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
            .uri(URI.create("https://serverless.roboflow.com/infer/workflows/ognjens-workspace-rujhp/detect-count-and-visualize-3"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();
        
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body());
    }
}