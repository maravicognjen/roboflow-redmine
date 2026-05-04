package com.example.roboflowredmine.controller;

import com.example.roboflowredmine.model.ProjectMapping;
import com.example.roboflowredmine.repository.ProjectMappingRepository;
import com.example.roboflowredmine.service.RedmineService;
import com.example.roboflowredmine.service.RoboflowService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Base64;
import java.util.List;

@Controller
public class IntegrationController {

    @Autowired
    private RoboflowService roboflowService;

    @Autowired
    private RedmineService redmineService;

    @Autowired
    private ProjectMappingRepository mappingRepo;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("mappings", mappingRepo.findAll());
        return "index";
    }

    @PostMapping("/link")
    public String linkProject(@RequestParam String roboflowProject,
                              @RequestParam Integer redmineProjectId,
                              RedirectAttributes ra) {
        ProjectMapping mapping = new ProjectMapping(roboflowProject, redmineProjectId);
        mappingRepo.save(mapping);
        ra.addFlashAttribute("message", "Povezan projekat: " + roboflowProject);
        return "redirect:/";
    }

    @PostMapping("/infer")
    public String infer(@RequestParam("imageFile") MultipartFile imageFile,
                        @RequestParam(required = false) String imageUrl,
                        @RequestParam(required = false) String modelId,
                        @RequestParam String roboflowProject,
                        RedirectAttributes ra) {
        try {
            JsonNode predictions;
            
            if (imageFile != null && !imageFile.isEmpty()) {
                byte[] imageBytes = imageFile.getBytes();
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                predictions = roboflowService.runWorkflow(base64Image);
            } else if (imageUrl != null && !imageUrl.isEmpty()) {
                predictions = roboflowService.runWorkflow(imageUrl);
            } else {
                throw new RuntimeException("Morate uneti ili upload slike ili URL slike");
            }

            List<ProjectMapping> mappings = mappingRepo.findByRoboflowProject(roboflowProject);
            if (mappings.isEmpty()) {
                throw new RuntimeException("Projekat nije povezan: " + roboflowProject);
            }
            ProjectMapping mapping = mappings.get(0);

            String subject = "Inference: " + (imageFile != null ? imageFile.getOriginalFilename() : imageUrl);
            String description = predictions.toPrettyString();
            Integer issueId = redmineService.createIssue(mapping.getRedmineProjectId(), subject, description);

            ra.addFlashAttribute("message", "Kreiran Redmine zadatak #" + issueId);
            ra.addFlashAttribute("predictions", description);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Greška: " + e.getMessage());
        }
        return "redirect:/";
    }

    
    @GetMapping("/test-redmine")
    @ResponseBody
    public String testRedmine() {
        try {
            Integer id = redmineService.createIssue(1, "Test from app", "Test description");
            return "Uspeh: Kreiran zadatak ID " + id;
        } catch (Exception e) {
            return "Greška: " + e.getMessage();
        }
    }
}