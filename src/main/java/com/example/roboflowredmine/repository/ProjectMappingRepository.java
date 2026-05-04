package com.example.roboflowredmine.repository;

import com.example.roboflowredmine.model.ProjectMapping;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProjectMappingRepository extends JpaRepository<ProjectMapping, Long> {
    List<ProjectMapping> findByRoboflowProject(String roboflowProject);
}
