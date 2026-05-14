package com.example.roboflowredmine.controller;

import com.example.roboflowredmine.dto.InferenceResultDTO;
import com.example.roboflowredmine.model.ProjectMapping;
import com.example.roboflowredmine.repository.ProjectMappingRepository;
import com.example.roboflowredmine.service.RedmineService;

import com.example.roboflowredmine.service.RoboflowService;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
public class IntegrationController {

    private static final Logger log = LoggerFactory.getLogger(IntegrationController.class);

    @Autowired
    private RoboflowService roboflowService;



    @Autowired
    private RedmineService redmineService;

    @Autowired
    private ProjectMappingRepository mappingRepo;

   
    @PostMapping("/link")
    public ResponseEntity<?> linkProject(
            @RequestParam String roboflowProject,
            @RequestParam Integer redmineProjectId) {

        
        ProjectMapping mapping = mappingRepo.findByRoboflowProject(roboflowProject)
                .stream()
                .findFirst()
                .orElse(new ProjectMapping(roboflowProject, redmineProjectId));

        mapping.setRedmineProjectId(redmineProjectId);
        mappingRepo.save(mapping);

        return ResponseEntity.ok(Map.of(
                "message", "Project linked",
                "roboflowProject", roboflowProject,
                "redmineProjectId", redmineProjectId
        ));
    }

    
    @PostMapping("/infer")
    public ResponseEntity<?> infer(
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam String roboflowProject,
            @RequestParam(required = false) String modelId) {

        
        if (imageFile.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Image file is empty"));
        }
        if (roboflowProject == null || roboflowProject.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Roboflow project alias is required"));
        }

        try {
            
            String base64 = roboflowService.fileToBase64(imageFile);

           
            JsonNode raw = roboflowService.runWorkflow(base64);

            
            InferenceResultDTO dto = roboflowService.parse(raw);

          
            List<ProjectMapping> mappings = mappingRepo.findByRoboflowProject(roboflowProject);
            if (mappings.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Project not linked. Please call /link first."));
            }
            ProjectMapping mapping = mappings.get(0);

           
            String effectiveModelId = (modelId != null && !modelId.isBlank()) ? modelId : "default-model";
            Integer issueId = redmineService.createFromInference(
                    dto,
                    effectiveModelId,
                    mapping.getRedmineProjectId()
            );

           
            return ResponseEntity.ok(Map.of(
                    "message", "OK",
                    "issueId", issueId,
                    "detections", dto.predictions, 
                    "image", "data:image/jpeg;base64," + base64
            ));

        } catch (Exception e) {
            log.error("Inference failed for project: {}", roboflowProject, e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Internal server error during inference"));
        }
    }

    
    @PostMapping("/export")
    public ResponseEntity<?> export(@RequestBody JsonNode inferenceResult) {
        return ResponseEntity.ok(inferenceResult);
    }

    
    @GetMapping("/mappings")
    public ResponseEntity<?> getMappings() {
        return ResponseEntity.ok(mappingRepo.findAll());
    }


}