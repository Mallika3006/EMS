
package com.mallika.EmployeeManagementSystem.controller;

import com.mallika.EmployeeManagementSystem.model.Project;
import com.mallika.EmployeeManagementSystem.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<Project> createProject(
            @RequestBody Project project,
            Authentication authentication) {

        Project created = projectService.createProject(
                project, authentication.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects(
            Authentication authentication) {

        return ResponseEntity.ok(
                projectService.getAllProjects(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(
            @PathVariable Integer id,
            Authentication authentication) {

        return ResponseEntity.ok(
                projectService.getProjectById(
                        id, authentication.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(
            @PathVariable Integer id,
            @RequestBody Project project,
            Authentication authentication) {

        return ResponseEntity.ok(
                projectService.updateProject(
                        id, project, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Integer id,
            Authentication authentication) {

        projectService.deleteProject(id, authentication.getName());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/manager/team")
    public ResponseEntity<List<Project>> getManagerTeamProjects(
            Authentication authentication) {

        return ResponseEntity.ok(
                projectService.getProjectsByManager(
                        authentication.getName()));
    }
}
