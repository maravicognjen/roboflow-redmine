package com.example.roboflowredmine.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ProjectMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String roboflowProject;
    private Integer redmineProjectId;

    public ProjectMapping() {}

    public ProjectMapping(String roboflowProject, Integer redmineProjectId) {
        this.roboflowProject = roboflowProject;
        this.redmineProjectId = redmineProjectId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoboflowProject() { return roboflowProject; }
    public void setRoboflowProject(String roboflowProject) { this.roboflowProject = roboflowProject; }
    public Integer getRedmineProjectId() { return redmineProjectId; }
    public void setRedmineProjectId(Integer redmineProjectId) { this.redmineProjectId = redmineProjectId; }
}