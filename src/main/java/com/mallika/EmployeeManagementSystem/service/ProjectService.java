package com.mallika.EmployeeManagementSystem.service;

import com.mallika.EmployeeManagementSystem.exception.ResourceNotFoundException;
import com.mallika.EmployeeManagementSystem.model.Project;
import com.mallika.EmployeeManagementSystem.model.User;
import com.mallika.EmployeeManagementSystem.repository.ProjectRepository;
import com.mallika.EmployeeManagementSystem.repository.UserRepository;
import org.springframework.stereotype.Service;

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
        Long companyId = getCompanyId(username);
        return projectRepository.createProject(project, companyId);
    }

    public List<Project> getAllProjects(String username) {
        Long companyId = getCompanyId(username);
        return projectRepository.getAllProjects(companyId);
    }

    public Project getProjectById(Integer id, String username) {
        Long companyId = getCompanyId(username);

        return projectRepository.getProjectById(id, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found with id: " + id));
    }

    public Project updateProject(
            Integer id, Project details, String username) {
        Long companyId = getCompanyId(username);

        return projectRepository.updateProject(
                id, details, companyId);
    }

    public void deleteProject(Integer id, String username) {
        Long companyId = getCompanyId(username);

        boolean deleted =
                projectRepository.deleteProject(id, companyId);

        if (!deleted) {
            throw new ResourceNotFoundException(
                    "Project not found with id: " + id);
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
}