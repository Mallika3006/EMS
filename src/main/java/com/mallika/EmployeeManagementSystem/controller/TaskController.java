package com.mallika.EmployeeManagementSystem.controller;

import com.mallika.EmployeeManagementSystem.model.Task;
import com.mallika.EmployeeManagementSystem.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Task> createTask(
            @RequestBody Task task) {

        return new ResponseEntity<>(
                taskService.createTask(task),
                HttpStatus.CREATED
        );
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {

        return ResponseEntity.ok(
                taskService.getAllTasks()
        );
    }

    // MY TASKS
    @GetMapping("/me")
    public ResponseEntity<List<Task>> getMyTasks(
            Authentication authentication) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                taskService.getMyTasks(username)
        );
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                taskService.getTaskById(id)
        );
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Integer id,
            @RequestBody Task task) {

        return ResponseEntity.ok(
                taskService.updateTask(id, task)
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Integer id) {

        taskService.deleteTask(id);

        return ResponseEntity.noContent().build();
    }

    // SEARCH BY NAME
    @GetMapping("/search")
    public ResponseEntity<List<Task>> searchByTaskName(
            @RequestParam String name) {

        return ResponseEntity.ok(
                taskService.searchByTaskName(name)
        );
    }

    // BY PROJECT
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Task>> getTasksByProject(
            @PathVariable Integer projectId) {

        return ResponseEntity.ok(
                taskService.getTasksByProject(projectId)
        );
    }

    // BY EMPLOYEE
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Task>> getTasksByEmployee(
            @PathVariable Integer employeeId) {

        return ResponseEntity.ok(
                taskService.getTasksByEmployee(employeeId)
        );
    }

    // BY STATUS
    @GetMapping("/status")
    public ResponseEntity<List<Task>> getTasksByStatus(
            @RequestParam String status) {

        return ResponseEntity.ok(
                taskService.getTasksByStatus(status)
        );
    }

    // BY PRIORITY
    @GetMapping("/priority")
    public ResponseEntity<List<Task>> getTasksByPriority(
            @RequestParam String priority) {

        return ResponseEntity.ok(
                taskService.getTasksByPriority(priority)
        );
    }

    // EMPLOYEE + STATUS
    @GetMapping("/employee/{employeeId}/status")
    public ResponseEntity<List<Task>> getEmployeeTasksByStatus(
            @PathVariable Integer employeeId,
            @RequestParam String status) {

        return ResponseEntity.ok(
                taskService.getEmployeeTasksByStatus(
                        employeeId,
                        status
                )
        );
    }

    // PROJECT + STATUS
    @GetMapping("/project/{projectId}/status")
    public ResponseEntity<List<Task>> getProjectTasksByStatus(
            @PathVariable Integer projectId,
            @RequestParam String status) {

        return ResponseEntity.ok(
                taskService.getProjectTasksByStatus(
                        projectId,
                        status
                )
        );
    }

    // BY DUE DATE
    @GetMapping("/due-date")
    public ResponseEntity<List<Task>> getTasksByDueDate(
            @RequestParam LocalDate dueDate) {

        return ResponseEntity.ok(
                taskService.getTasksByDueDate(dueDate)
        );
    }

    // BETWEEN DUE DATES
    @GetMapping("/between")
    public ResponseEntity<List<Task>> getTasksBetweenDueDates(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        return ResponseEntity.ok(
                taskService.getTasksBetweenDueDates(
                        startDate,
                        endDate
                )
        );
    }

    // OVERDUE TASKS
    @GetMapping("/overdue")
    public ResponseEntity<List<Task>> getOverdueTasks(
            @RequestParam LocalDate date) {

        return ResponseEntity.ok(
                taskService.getOverdueTasks(date)
        );
    }

    // SORT
    @GetMapping("/sort")
    public ResponseEntity<List<Task>> sortTasks(
            @RequestParam String field,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(
                taskService.sortTasks(field, direction)
        );
    }
}