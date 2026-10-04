package com.mallika.EmployeeManagementSystem.service;

import com.mallika.EmployeeManagementSystem.exception.ResourceNotFoundException;
import com.mallika.EmployeeManagementSystem.model.Project;
import com.mallika.EmployeeManagementSystem.model.User;
import com.mallika.EmployeeManagementSystem.repository.ProjectRepository;
import com.mallika.EmployeeManagementSystem.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    // CREATE
    public Project createProject(Project project) {
        return projectRepository.createProject(project);
    }

    // GET ALL
    public List<Project> getAllProjects() {
        return projectRepository.getAllProjects();
    }

    // GET BY ID
    public Project getProjectById(Integer id) {

        return projectRepository.getProjectById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found with id: " + id
                        ));
    }

    // UPDATE
    public Project updateProject(
            Integer id,
            Project projectDetails) {

        return projectRepository.updateProject(
                id,
                projectDetails
        );
    }

    // DELETE
    public void deleteProject(Integer id) {

        boolean deleted = projectRepository.deleteProject(id);

        if (!deleted) {
            throw new ResourceNotFoundException(
                    "Project not found with id: " + id
            );
        }
    }

    // SEARCH BY NAME
    public List<Project> searchByProjectName(String projectName) {
        return projectRepository.searchByProjectName(projectName);
    }

    // EXACT NAME
    public List<Project> getByExactProjectName(
            String projectName) {

        return projectRepository.getProjectsByExactName(
                projectName
        );
    }

    // START DATE
    public List<Project> getByStartDate(
            LocalDate startDate) {

        return projectRepository.getByStartDate(startDate);
    }

    // END DATE
    public List<Project> getByEndDate(
            LocalDate endDate) {

        return projectRepository.getByEndDate(endDate);
    }

    // PROJECTS BETWEEN DATES
    public List<Project> getProjectsBetweenDates(
            LocalDate startDate,
            LocalDate endDate) {

        return projectRepository.getProjectsBetweenDates(
                startDate,
                endDate
        );
    }

    // ONGOING PROJECTS
    public List<Project> getOngoingProjects() {
        return projectRepository.getOngoingProjects();
    }

    // SORT
    public List<Project> sortProjects(
            String field,
            String direction) {

        return projectRepository.sortProjects(
                field,
                direction
        );
    }

    // GET PROJECTS OF LOGGED-IN EMPLOYEE
    public List<Project> getMyProjects(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + username
                        ));

        if (user.getEmployee() == null ||
                user.getEmployee().getEmployeeId() == null) {

            throw new ResourceNotFoundException(
                    "Employee not assigned to this user"
            );
        }

        Integer employeeId =
                user.getEmployee().getEmployeeId();

        return projectRepository.getProjectsByEmployeeId(
                employeeId
        );
    }
}