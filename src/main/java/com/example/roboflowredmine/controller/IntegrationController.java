package com.example.roboflowredmine.controller;

import com.example.roboflowredmine.dto.InferenceResultDTO;
import com.example.roboflowredmine.model.ProjectMapping;
import com.example.roboflowredmine.repository.ProjectMappingRepository;
import com.example.roboflowredmine.service.RedmineService;
import com.example.roboflowredmine.service.RoboflowDatasetService;
import com.example.roboflowredmine.service.RoboflowService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
public class IntegrationController {

   
    @Autowired
    private RoboflowService roboflowService;

    @Autowired
    private RoboflowDatasetService datasetService;

    @Autowired
    private RedmineService redmineService;

    @Autowired
    private ProjectMappingRepository mappingRepo;

    @PostMapping("/link")
    public ResponseEntity<?> linkProject(
            @RequestParam String roboflowProject,
            @RequestParam Integer redmineProjectId) {

        ProjectMapping mapping =
                new ProjectMapping(roboflowProject, redmineProjectId);

        mappingRepo.save(mapping);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Project linked",
                        "roboflowProject", roboflowProject,
                        "redmineProjectId", redmineProjectId
                )
        );
    }


    @PostMapping("/infer")
    public ResponseEntity<?> infer(
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam String roboflowProject,
            @RequestParam(required = false) String modelId) {

        try {
           
            String base64 = roboflowService.fileToBase64(imageFile);

            
            JsonNode raw = roboflowService.runWorkflow(base64);

          
            InferenceResultDTO dto = roboflowService.parse(raw);

           
            List<ProjectMapping> mappings =
                    mappingRepo.findByRoboflowProject(roboflowProject);

            if (mappings.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Project not linked"));
            }

            ProjectMapping mapping = mappings.get(0);

           
            Integer issueId = redmineService.createFromInference(
                    dto,
                    modelId != null ? modelId : "model-1",
                    mapping.getRedmineProjectId()
            );

          
            return ResponseEntity.ok(
                    Map.of(
                            "message", "OK",
                            "issueId", issueId,
                            "detections", dto.predictions,
                            "image", "data:image/jpeg;base64," + base64
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/export")
    public ResponseEntity<?> export(@RequestBody JsonNode inferenceResult) {
        return ResponseEntity.ok(inferenceResult);
    }


    @GetMapping("/test-redmine")
    public ResponseEntity<?> testRedmine() {

        try {
            Integer id = redmineService.createIssue(
                    1,
                    "Test issue",
                    "This is a test from system"
            );

            return ResponseEntity.ok(
                    Map.of("message", "Success", "issueId", id)
            );

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

   
    @GetMapping("/mappings")
    public ResponseEntity<?> getMappings() {
        return ResponseEntity.ok(mappingRepo.findAll());
    }


  
    @GetMapping("/projects")
    public ResponseEntity<?> getProjects() throws Exception {
        return ResponseEntity.ok(datasetService.getProjects());
    }

   
    @GetMapping("/projects/{id}")
    public ResponseEntity<?> getProject(@PathVariable String id) throws Exception {
        return ResponseEntity.ok(datasetService.getProjectDetails(id));
    }

    
    @GetMapping("/projects/{id}/versions")
    public ResponseEntity<?> getVersions(@PathVariable String id) throws Exception {
        return ResponseEntity.ok(datasetService.getVersions(id));
    }

    
    @PostMapping("/projects/{id}/versions")
    public ResponseEntity<?> createVersion(@PathVariable String id) throws Exception {
        return ResponseEntity.ok(datasetService.createVersion(id));
    }

    @PostMapping("/projects/{id}/images")
    public ResponseEntity<?> uploadImage(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) throws Exception {

        String base64 = roboflowService.fileToBase64(file);

        return ResponseEntity.ok(
                datasetService.uploadImage(id, base64)
        );
    }

 
    @GetMapping("/projects/{id}/images")
    public ResponseEntity<?> listImages(@PathVariable String id) throws Exception {

        return ResponseEntity.ok(
                datasetService.listImages(id)
        );
    }
}