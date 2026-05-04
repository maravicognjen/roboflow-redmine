package com.example.roboflowredmine.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class RoboflowDatasetService {

    @Value("${roboflow.api.key}")
    private String apiKey;

    @Value("${roboflow.workspace}")
    private String workspace;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();


    public JsonNode getProjects() throws Exception {

        String url = "https://api.roboflow.com/" + workspace + "?api_key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return mapper.readTree(response.body());
    }

  
    public JsonNode getProjectDetails(String projectId) throws Exception {

        String url = "https://api.roboflow.com/" + workspace + "/" + projectId
                + "?api_key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return mapper.readTree(response.body());
    }


    public JsonNode getVersions(String projectId) throws Exception {

        String url = "https://api.roboflow.com/" + workspace + "/"
                + projectId + "?api_key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return mapper.readTree(response.body());
    }

  
    public JsonNode createVersion(String projectId) throws Exception {

        String url = "https://api.roboflow.com/" + workspace + "/"
                + projectId + "/versions?api_key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return mapper.readTree(response.body());
    }
    public JsonNode uploadImage(String projectId, String base64Image) throws Exception {

        String url = "https://api.roboflow.com/" + workspace + "/"
                + projectId + "/upload?api_key=" + apiKey;

        String body = """
        {
          "image": "%s"
        }
        """.formatted(base64Image);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return mapper.readTree(response.body());
    }
    public JsonNode listImages(String projectId) throws Exception {

        String url = "https://api.roboflow.com/" + workspace + "/"
                + projectId + "/images?api_key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());
        

        return mapper.readTree(response.body());
    }
    
}
