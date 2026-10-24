package com.mallika.EmployeeManagementSystem.service;

import com.mallika.EmployeeManagementSystem.exception.ResourceNotFoundException;
import com.mallika.EmployeeManagementSystem.model.Project;
import com.mallika.EmployeeManagementSystem.model.User;
import com.mallika.EmployeeManagementSystem.repository.ProjectRepository;
import com.mallika.EmployeeManagementSystem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    private Long getCompanyId(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + username));

        if (user.getCompanyId() == null) {
            throw new IllegalStateException(
                    "No company is assigned to this user");
        }

        return user.getCompanyId();
    }

    public Project createProject(Project project, String username) {
        requireAdmin();
        User user = getUser(username);
        Long companyId = requireCompanyId(user);
        validateProject(project);
        return projectRepository.createProject(project, companyId);
    }

    public List<Project> getAllProjects(String username) {
        User user = getUser(username);
        Long companyId = requireCompanyId(user);
        if (isManager()) {
            return projectRepository.getProjectsByManager(
                    requireEmployeeId(user), companyId);
        }
        return projectRepository.getAllProjects(companyId);
    }

    public Project getProjectById(Integer id, String username) {
        User user = getUser(username);
        Long companyId = requireCompanyId(user);

        Project project = projectRepository.getProjectById(id, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found with id: " + id));

        if (isManager() && !requireEmployeeId(user).equals(project.getManagerId())) {
            throw new ResourceNotFoundException(
                    "Project not found in your managed projects");
        }
        return project;
    }

    public Project updateProject(
            Integer id, Project details, String username) {
        requireAdmin();
        User user = getUser(username);
        Long companyId = requireCompanyId(user);
        validateProject(details);
        return projectRepository.updateProject(id, details, companyId);
    }

    public void deleteProject(Integer id, String username) {
        requireAdmin();
        User user = getUser(username);
        Long companyId = requireCompanyId(user);

        if (projectRepository.hasTasks(id, companyId)) {
            throw new IllegalStateException(
                    "Delete or reassign the project's tasks before deleting the project");
        }

        boolean deleted =
                projectRepository.deleteProject(id, companyId);

        if (!deleted) {
            throw new ResourceNotFoundException(
                    "Project not found with id: " + id);
        }
    }

    public void assignProjectToEmployee(
            Integer projectId, Integer employeeId, String username) {
        if (!isManager()) {
            throw new AccessDeniedException(
                    "Only a Manager can assign projects to their direct reports");
        }

        User manager = getUser(username);
        Long companyId = requireCompanyId(manager);
        Integer managerId = requireEmployeeId(manager);

        boolean assigned = projectRepository.assignProjectToEmployee(
                projectId, employeeId, managerId, companyId);
        if (!assigned) {
            throw new ResourceNotFoundException(
                    "Project or employee not found in your company, or employee is not your direct report");
        }
    }

    public List<Project> getProjectsByManager(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + username));

        if (user.getCompanyId() == null
                || user.getEmployee() == null
                || user.getEmployee().getEmployeeId() == null) {
            throw new IllegalStateException(
                    "User must have a company and employee assigned");
        }

        return projectRepository.getProjectsByManager(
                user.getEmployee().getEmployeeId(),
                user.getCompanyId());
    }

    public List<Project> getMyProjects(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + username));

        if (user.getCompanyId() == null
                || user.getEmployee() == null
                || user.getEmployee().getEmployeeId() == null) {
            throw new IllegalStateException(
                    "User must have a company and employee assigned");
        }

        return projectRepository.getProjectsByEmployee(
                user.getEmployee().getEmployeeId(),
                user.getCompanyId());
    }


    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + username));
    }

    private Long requireCompanyId(User user) {
        if (user.getCompanyId() == null) {
            throw new IllegalStateException("No company is assigned to this user");
        }
        return user.getCompanyId();
    }

    private Integer requireEmployeeId(User user) {
        if (user.getEmployee() == null || user.getEmployee().getEmployeeId() == null) {
            throw new ResourceNotFoundException("Employee is not assigned to this user");
        }
        return user.getEmployee().getEmployeeId();
    }

    private void requireAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean admin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (!admin) {
            throw new AccessDeniedException("Only an Admin can create, edit, or delete projects");
        }
    }

    private void validateProject(Project project) {
        if (project == null || project.getProjectName() == null
                || project.getProjectName().trim().isEmpty()) {
            throw new IllegalArgumentException("Project name is required");
        }
        if (project.getProjectName().trim().length() > 150) {
            throw new IllegalArgumentException("Project name cannot exceed 150 characters");
        }
        if (project.getManagerId() == null) {
            throw new IllegalArgumentException("Select a project manager");
        }
        if (project.getStartDate() != null && project.getEndDate() != null
                && project.getEndDate().isBefore(project.getStartDate())) {
            throw new IllegalArgumentException("Project end date cannot be before its start date");
        }
        if (project.getStatus() == null || project.getStatus().isBlank()) {
            project.setStatus("PLANNED");
        } else {
            project.setStatus(project.getStatus().trim().toUpperCase());
        }
        if (!List.of("PLANNED", "IN_PROGRESS", "ON_HOLD", "COMPLETED").contains(project.getStatus())) {
            throw new IllegalArgumentException("Project status must be PLANNED, IN_PROGRESS, ON_HOLD, or COMPLETED");
        }
        if (project.getPriority() == null || project.getPriority().isBlank()) {
            project.setPriority("MEDIUM");
        } else {
            project.setPriority(project.getPriority().trim().toUpperCase());
        }
        if (!List.of("LOW", "MEDIUM", "HIGH", "CRITICAL").contains(project.getPriority())) {
            throw new IllegalArgumentException("Project priority must be LOW, MEDIUM, HIGH, or CRITICAL");
        }
        if (project.getDescription() != null && project.getDescription().length() > 4000) {
            throw new IllegalArgumentException("Project description cannot exceed 4000 characters");
        }
    }

    private boolean isManager() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_MANAGER".equals(authority.getAuthority()));
    }


    private void ensureManagerOwnsProject(Integer projectId, User user, Long companyId) {
        Integer managerId = requireEmployeeId(user);
        Project project = projectRepository.getProjectById(projectId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found in your company: " + projectId));
        if (!managerId.equals(project.getManagerId())) {
            throw new AccessDeniedException("Managers can only access projects assigned to them");
        }
    }

}