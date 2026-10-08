package com.mallika.EmployeeManagementSystem.controller;

import com.mallika.EmployeeManagementSystem.model.Project;
import com.mallika.EmployeeManagementSystem.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Project> createProject(
            @RequestBody Project project) {

        return new ResponseEntity<>(
                projectService.createProject(project),
                HttpStatus.CREATED
        );
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {

        return ResponseEntity.ok(
                projectService.getAllProjects()
        );
    }

    @GetMapping("/me")
    public ResponseEntity<List<Project>> getMyProjects(
            Authentication authentication) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                projectService.getMyProjects(username)
        );
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                projectService.getProjectById(id)
        );
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(
            @PathVariable Integer id,
            @RequestBody Project project) {

        return ResponseEntity.ok(
                projectService.updateProject(id, project)
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Integer id) {

        projectService.deleteProject(id);

        return ResponseEntity.noContent().build();
    }

    // SEARCH BY NAME
    @GetMapping("/search")
    public ResponseEntity<List<Project>> searchByProjectName(
            @RequestParam String name) {

        return ResponseEntity.ok(
                projectService.searchByProjectName(name)
        );
    }

    // EXACT PROJECT NAME
    @GetMapping("/name")
    public ResponseEntity<List<Project>> getByExactProjectName(
            @RequestParam String name) {

        return ResponseEntity.ok(
                projectService.getByExactProjectName(name)
        );
    }

    // START DATE
    @GetMapping("/start-date")
    public ResponseEntity<List<Project>> getByStartDate(
            @RequestParam LocalDate startDate) {

        return ResponseEntity.ok(
                projectService.getByStartDate(startDate)
        );
    }

    // END DATE
    @GetMapping("/end-date")
    public ResponseEntity<List<Project>> getByEndDate(
            @RequestParam LocalDate endDate) {

        return ResponseEntity.ok(
                projectService.getByEndDate(endDate)
        );
    }

    // PROJECTS BETWEEN DATES
    @GetMapping("/between")
    public ResponseEntity<List<Project>> getProjectsBetweenDates(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        return ResponseEntity.ok(
                projectService.getProjectsBetweenDates(
                        startDate,
                        endDate
                )
        );
    }

    // ONGOING PROJECTS
    @GetMapping("/ongoing")
    public ResponseEntity<List<Project>> getOngoingProjects() {

        return ResponseEntity.ok(
                projectService.getOngoingProjects()
        );
    }

    // SORT
    @GetMapping("/sort")
    public ResponseEntity<List<Project>> sortProjects(
            @RequestParam String field,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(
                projectService.sortProjects(field, direction)
        );
    }

    // GET MANAGER TEAM PROJECTS
    @GetMapping("/manager/team")
    public ResponseEntity<List<Project>> getTeamProjects(
            Authentication authentication) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                projectService.getTeamProjects(username)
        );
    }
}
