package com.mallika.EmployeeManagementSystem.service;

import com.mallika.EmployeeManagementSystem.exception.ResourceNotFoundException;
import com.mallika.EmployeeManagementSystem.model.Task;
import com.mallika.EmployeeManagementSystem.model.User;
import com.mallika.EmployeeManagementSystem.repository.TaskRepository;
import com.mallika.EmployeeManagementSystem.repository.UserRepository;
import org.springframework.stereotype.Service;

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

        return taskRepository.createTask(task, user.getCompanyId());
    }


    // =========================================================
    // GET ALL
    // =========================================================

    public List<Task> getAllTasks() {
        return taskRepository.getAllTasks();
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    public Task getTaskById(Integer id) {

        return taskRepository.getTaskById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found with id: " + id
                        ));
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

        return taskRepository
                .searchByTaskName(taskName);
    }


    // =========================================================
    // BY PROJECT
    // =========================================================

    public List<Task> getTasksByProject(
            Integer projectId) {

        return taskRepository
                .findByProjectProjectId(projectId);
    }


    // =========================================================
    // BY EMPLOYEE
    // =========================================================

    public List<Task> getTasksByEmployee(
            Integer employeeId) {

        return taskRepository
                .findByAssignedToEmployeeId(employeeId);
    }


    // =========================================================
    // BY STATUS
    // =========================================================

    public List<Task> getTasksByStatus(
            String status) {

        return taskRepository
                .findByStatusIgnoreCase(status);
    }


    // =========================================================
    // BY PRIORITY
    // =========================================================

    public List<Task> getTasksByPriority(
            String priority) {

        return taskRepository
                .findByPriorityIgnoreCase(priority);
    }


    // =========================================================
    // EMPLOYEE + STATUS
    // =========================================================

    public List<Task> getEmployeeTasksByStatus(
            Integer employeeId,
            String status) {

        return taskRepository
                .findByAssignedToEmployeeIdAndStatusIgnoreCase(
                        employeeId,
                        status
                );
    }


    // =========================================================
    // PROJECT + STATUS
    // =========================================================

    public List<Task> getProjectTasksByStatus(
            Integer projectId,
            String status) {

        return taskRepository
                .findByProjectProjectIdAndStatusIgnoreCase(
                        projectId,
                        status
                );
    }


    // =========================================================
    // BY DUE DATE
    // =========================================================

    public List<Task> getTasksByDueDate(
            LocalDate dueDate) {

        return taskRepository
                .findByDueDate(dueDate);
    }


    // =========================================================
    // BETWEEN DUE DATES
    // =========================================================

    public List<Task> getTasksBetweenDueDates(
            LocalDate startDate,
            LocalDate endDate) {

        return taskRepository
                .findByDueDateBetween(
                        startDate,
                        endDate
                );
    }


    // =========================================================
    // OVERDUE TASKS
    // =========================================================

    public List<Task> getOverdueTasks(
            LocalDate date) {

        return taskRepository
                .findByDueDateBeforeAndStatusNotIgnoreCase(
                        date,
                        "Completed"
                );
    }


    // =========================================================
    // SORT
    // =========================================================

    public List<Task> sortTasks(
            String field,
            String direction) {

        return taskRepository
                .sortTasks(
                        field,
                        direction
                );
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

        return taskRepository
                .findByAssignedToEmployeeId(employeeId);
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

        return taskRepository
                .getTasksByManagerId(managerId);
    }
}