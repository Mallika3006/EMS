package com.mallika.EmployeeManagementSystem.service;

import com.mallika.EmployeeManagementSystem.exception.ResourceNotFoundException;
import com.mallika.EmployeeManagementSystem.model.Task;
import com.mallika.EmployeeManagementSystem.model.User;
import com.mallika.EmployeeManagementSystem.repository.TaskRepository;
import com.mallika.EmployeeManagementSystem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(
            TaskRepository taskRepository,
            UserRepository userRepository) {

        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    // =========================================================
    // CREATE
    // =========================================================

    public Task createTask(Task task, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + username));

        if (user.getCompanyId() == null) {
            throw new IllegalStateException(
                    "No company is assigned to this user");
        }

        if (isManager()) {
            Integer managerId = requireEmployeeId(user);
            validateManagerTaskAssignments(task, managerId, user.getCompanyId());
        }

        return taskRepository.createTask(task, user.getCompanyId());
    }


    // =========================================================
    // GET ALL
    // =========================================================

    public List<Task> getAllTasks() {
        User user = currentUser();
        if (isManager()) {
            return taskRepository.getTasksByManagerId(requireEmployeeId(user));
        }
        return taskRepository.getAllTasks();
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    public Task getTaskById(Integer id) {

        Task task = taskRepository.getTaskById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found with id: " + id
                        ));
        ensureManagerTaskAccess(id);
        return task;
    }


    // =========================================================
    // UPDATE
    // =========================================================

    public Task updateTask(Integer id, Task taskDetails, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found: " + username));

        Long companyId = user.getCompanyId();

        if (companyId == null) {
            throw new IllegalStateException("User has no company assigned");
        }

        ensureManagerTaskAccess(id);
        if (isManager()) {
            validateManagerTaskAssignments(
                    taskDetails, requireEmployeeId(user), companyId);
        }
        return taskRepository.updateTask(id, taskDetails, companyId);
    }


    // =========================================================
    // DELETE
    // =========================================================

    public void deleteTask(Integer id, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found: " + username));

        Long companyId = user.getCompanyId();

        if (companyId == null) {
            throw new IllegalStateException("User has no company assigned");
        }

        ensureManagerTaskAccess(id);
        boolean deleted = taskRepository.deleteTask(id, companyId);

        if (!deleted) {
            throw new ResourceNotFoundException(
                    "Task not found with id: " + id
            );
        }
    }

    // =========================================================
    // SEARCH BY TASK NAME
    // =========================================================

    public List<Task> searchByTaskName(
            String taskName) {

        return scopeManagerTasks(taskRepository.searchByTaskName(taskName));
    }


    // =========================================================
    // BY PROJECT
    // =========================================================

    public List<Task> getTasksByProject(
            Integer projectId) {

        return scopeManagerTasks(taskRepository.findByProjectProjectId(projectId));
    }


    // =========================================================
    // BY EMPLOYEE
    // =========================================================

    public List<Task> getTasksByEmployee(
            Integer employeeId) {

        return scopeManagerTasks(taskRepository.findByAssignedToEmployeeId(employeeId));
    }


    // =========================================================
    // BY STATUS
    // =========================================================

    public List<Task> getTasksByStatus(
            String status) {

        return scopeManagerTasks(taskRepository.findByStatusIgnoreCase(status));
    }


    // =========================================================
    // BY PRIORITY
    // =========================================================

    public List<Task> getTasksByPriority(
            String priority) {

        return scopeManagerTasks(taskRepository.findByPriorityIgnoreCase(priority));
    }


    // =========================================================
    // EMPLOYEE + STATUS
    // =========================================================

    public List<Task> getEmployeeTasksByStatus(
            Integer employeeId,
            String status) {

        return scopeManagerTasks(taskRepository.findByAssignedToEmployeeIdAndStatusIgnoreCase(employeeId, status));
    }


    // =========================================================
    // PROJECT + STATUS
    // =========================================================

    public List<Task> getProjectTasksByStatus(
            Integer projectId,
            String status) {

        return scopeManagerTasks(taskRepository.findByProjectProjectIdAndStatusIgnoreCase(projectId, status));
    }


    // =========================================================
    // BY DUE DATE
    // =========================================================

    public List<Task> getTasksByDueDate(
            LocalDate dueDate) {

        return scopeManagerTasks(taskRepository.findByDueDate(dueDate));
    }


    // =========================================================
    // BETWEEN DUE DATES
    // =========================================================

    public List<Task> getTasksBetweenDueDates(
            LocalDate startDate,
            LocalDate endDate) {

        return scopeManagerTasks(taskRepository.findByDueDateBetween(startDate, endDate));
    }


    // =========================================================
    // OVERDUE TASKS
    // =========================================================

    public List<Task> getOverdueTasks(
            LocalDate date) {

        return scopeManagerTasks(taskRepository.findByDueDateBeforeAndStatusNotIgnoreCase(date, "Completed"));
    }


    // =========================================================
    // SORT
    // =========================================================

    public List<Task> sortTasks(
            String field,
            String direction) {

        return scopeManagerTasks(taskRepository.sortTasks(field, direction));
    }


    // =========================================================
    // GET TASKS ASSIGNED TO LOGGED-IN EMPLOYEE
    // =========================================================

    public List<Task> getMyTasks(String username) {

        User user =
                userRepository
                        .findByUsername(username)
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

        return scopeManagerTasks(taskRepository.findByAssignedToEmployeeId(employeeId));
    }

    // =========================================================
// GET TEAM TASKS
// =========================================================

    public List<Task> getTeamTasks(String username) {

        User user =
                userRepository
                        .findByUsername(username)
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

        Integer managerId =
                user.getEmployee().getEmployeeId();

        return scopeManagerTasks(taskRepository.getTasksByManagerId(managerId));
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new AccessDeniedException("Authenticated user required");
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + authentication.getName()));
    }

    private boolean isManager() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_MANAGER".equals(authority.getAuthority()));
    }

    private Integer requireEmployeeId(User user) {
        if (user.getEmployee() == null || user.getEmployee().getEmployeeId() == null) {
            throw new ResourceNotFoundException("Employee is not assigned to this user");
        }
        return user.getEmployee().getEmployeeId();
    }

    private void ensureManagerTaskAccess(Integer taskId) {
        if (!isManager()) return;
        User user = currentUser();
        Integer managerId = requireEmployeeId(user);
        if (user.getCompanyId() == null
                || !taskRepository.isTaskAccessibleToManager(
                        taskId, managerId, user.getCompanyId())) {
            throw new ResourceNotFoundException(
                    "Task not found in your managed projects or team");
        }
    }

    private void validateManagerTaskAssignments(
            Task task, Integer managerId, Long companyId) {
        if (task == null) {
            throw new IllegalArgumentException("Task details are required");
        }

        Integer projectId = task.getProject() == null
                ? null : task.getProject().getProjectId();
        Integer employeeId = task.getAssignedTo() == null
                ? null : task.getAssignedTo().getEmployeeId();

        // Keep manager-created tasks connected to their visible project/team scope.
        if (projectId == null && employeeId == null) {
            throw new IllegalArgumentException(
                    "Select one of your projects or assign the task to an active direct report.");
        }

        if (projectId != null
                && !taskRepository.isProjectManagedBy(projectId, managerId, companyId)) {
            throw new AccessDeniedException(
                    "Managers can only assign tasks to their own projects");
        }
        if (employeeId != null
                && !taskRepository.isEmployeeManagedBy(employeeId, managerId, companyId)) {
            throw new AccessDeniedException(
                    "Managers can only assign tasks to their direct reports");
        }
    }

    private List<Task> scopeManagerTasks(List<Task> tasks) {
        if (!isManager()) return tasks;
        User user = currentUser();
        Integer managerId = requireEmployeeId(user);
        Long companyId = user.getCompanyId();
        if (companyId == null) {
            throw new AccessDeniedException("No company assigned to manager");
        }
        return tasks.stream()
                .filter(task -> task.getTaskId() != null
                        && taskRepository.isTaskAccessibleToManager(
                                task.getTaskId(), managerId, companyId))
                .toList();
    }

}