package com.mallika.EmployeeManagementSystem.repository;

import com.mallika.EmployeeManagementSystem.model.Employee;
import com.mallika.EmployeeManagementSystem.model.Project;
import com.mallika.EmployeeManagementSystem.model.Task;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TaskRepository {

    private final DataSource dataSource;

    public TaskRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // =========================================================
    // CREATE
    // =========================================================

    public Task createTask(Task task) {

        String sql =
                "SELECT * FROM create_task(?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setString(1, task.getTaskName());
            statement.setString(2, task.getDescription());
            statement.setString(3, task.getPriority());
            statement.setString(4, task.getStatus());

            if (task.getStartDate() != null) {
                statement.setDate(
                        5,
                        Date.valueOf(task.getStartDate())
                );
            } else {
                statement.setNull(
                        5,
                        java.sql.Types.DATE
                );
            }

            if (task.getDueDate() != null) {
                statement.setDate(
                        6,
                        Date.valueOf(task.getDueDate())
                );
            } else {
                statement.setNull(
                        6,
                        java.sql.Types.DATE
                );
            }

            if (task.getProject() != null) {

                statement.setInt(
                        7,
                        task.getProject().getProjectId()
                );

            } else {

                statement.setNull(
                        7,
                        java.sql.Types.INTEGER
                );
            }

            if (task.getAssignedTo() != null) {

                statement.setInt(
                        8,
                        task.getAssignedTo().getEmployeeId()
                );

            } else {

                statement.setNull(
                        8,
                        java.sql.Types.INTEGER
                );
            }

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapTask(resultSet);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error creating task",
                    e
            );
        }

        throw new RuntimeException(
                "Task could not be created"
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    public List<Task> getAllTasks() {

        List<Task> tasks = new ArrayList<>();

        String sql =
                "SELECT * FROM get_all_tasks()";

        try (Connection connection =
                     dataSource.getConnection();

             CallableStatement statement =
                     connection.prepareCall(sql);

             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {

                tasks.add(
                        mapTask(resultSet)
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching tasks",
                    e
            );
        }

        return tasks;
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    public Optional<Task> getTaskById(
            Integer id) {

        String sql =
                "SELECT * FROM get_task_by_id(?)";

        try (Connection connection =
                     dataSource.getConnection();

             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {

                    return Optional.of(
                            mapTask(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching task with id: "
                            + id,
                    e
            );
        }

        return Optional.empty();
    }


    // =========================================================
    // UPDATE
    // =========================================================

    public Task updateTask(
            Integer id,
            Task taskDetails) {

        String sql =
                "SELECT * FROM update_task(?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection =
                     dataSource.getConnection();

             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, id);

            statement.setString(
                    2,
                    taskDetails.getTaskName()
            );

            statement.setString(
                    3,
                    taskDetails.getDescription()
            );

            statement.setString(
                    4,
                    taskDetails.getPriority()
            );

            statement.setString(
                    5,
                    taskDetails.getStatus()
            );

            if (taskDetails.getStartDate() != null) {

                statement.setDate(
                        6,
                        Date.valueOf(
                                taskDetails.getStartDate()
                        )
                );

            } else {

                statement.setNull(
                        6,
                        java.sql.Types.DATE
                );
            }

            if (taskDetails.getDueDate() != null) {

                statement.setDate(
                        7,
                        Date.valueOf(
                                taskDetails.getDueDate()
                        )
                );

            } else {

                statement.setNull(
                        7,
                        java.sql.Types.DATE
                );
            }

            if (taskDetails.getProject() != null) {

                statement.setInt(
                        8,
                        taskDetails
                                .getProject()
                                .getProjectId()
                );

            } else {

                statement.setNull(
                        8,
                        java.sql.Types.INTEGER
                );
            }

            if (taskDetails.getAssignedTo() != null) {

                statement.setInt(
                        9,
                        taskDetails
                                .getAssignedTo()
                                .getEmployeeId()
                );

            } else {

                statement.setNull(
                        9,
                        java.sql.Types.INTEGER
                );
            }

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {

                    return mapTask(resultSet);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error updating task with id: "
                            + id,
                    e
            );
        }

        throw new RuntimeException(
                "Task not found with id: " + id
        );
    }


    // =========================================================
    // DELETE
    // =========================================================

    public boolean deleteTask(Integer id) {

        String sql =
                "SELECT delete_task(?)";

        try (Connection connection =
                     dataSource.getConnection();

             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {

                    return resultSet.getBoolean(1);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error deleting task with id: "
                            + id,
                    e
            );
        }

        return false;
    }


    // =========================================================
    // SEARCH BY TASK NAME
    // =========================================================

    public List<Task> searchByTaskName(
            String taskName) {

        List<Task> tasks =
                new ArrayList<>();

        String sql =
                "SELECT * FROM search_tasks_by_name(?)";

        try (Connection connection =
                     dataSource.getConnection();

             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setString(1, taskName);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    tasks.add(
                            mapTask(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error searching tasks by name",
                    e
            );
        }

        return tasks;
    }


    // =========================================================
    // BY PROJECT
    // =========================================================

    public List<Task> findByProjectProjectId(
            Integer projectId) {

        List<Task> tasks =
                new ArrayList<>();

        String sql =
                "SELECT * FROM get_tasks_by_project(?)";

        try (Connection connection =
                     dataSource.getConnection();

             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, projectId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    tasks.add(
                            mapTask(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching tasks by project",
                    e
            );
        }

        return tasks;
    }


    // =========================================================
    // BY EMPLOYEE
    // =========================================================

    public List<Task> findByAssignedToEmployeeId(
            Integer employeeId) {

        List<Task> tasks =
                new ArrayList<>();

        String sql =
                "SELECT * FROM get_tasks_by_employee(?)";

        try (Connection connection =
                     dataSource.getConnection();

             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, employeeId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    tasks.add(
                            mapTask(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching tasks by employee",
                    e
            );
        }

        return tasks;
    }


    // =========================================================
    // BY STATUS
    // =========================================================

    public List<Task> findByStatusIgnoreCase(
            String status) {

        List<Task> tasks =
                new ArrayList<>();

        String sql =
                "SELECT * FROM get_tasks_by_status(?)";

        try (Connection connection =
                     dataSource.getConnection();

             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setString(1, status);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    tasks.add(
                            mapTask(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching tasks by status",
                    e
            );
        }

        return tasks;
    }


    // =========================================================
    // BY PRIORITY
    // =========================================================

    public List<Task> findByPriorityIgnoreCase(
            String priority) {

        List<Task> tasks =
                new ArrayList<>();

        String sql =
                "SELECT * FROM get_tasks_by_priority(?)";

        try (Connection connection =
                     dataSource.getConnection();

             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setString(1, priority);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    tasks.add(
                            mapTask(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching tasks by priority",
                    e
            );
        }

        return tasks;
    }


    // =========================================================
    // EMPLOYEE + STATUS
    // =========================================================

    public List<Task>
    findByAssignedToEmployeeIdAndStatusIgnoreCase(
            Integer employeeId,
            String status) {

        List<Task> tasks =
                new ArrayList<>();

        String sql =
                "SELECT * FROM get_employee_tasks_by_status(?, ?)";

        try (Connection connection =
                     dataSource.getConnection();

             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, employeeId);
            statement.setString(2, status);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    tasks.add(
                            mapTask(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching employee tasks by status",
                    e
            );
        }

        return tasks;
    }


    // =========================================================
    // PROJECT + STATUS
    // =========================================================

    public List<Task>
    findByProjectProjectIdAndStatusIgnoreCase(
            Integer projectId,
            String status) {

        List<Task> tasks =
                new ArrayList<>();

        String sql =
                "SELECT * FROM get_project_tasks_by_status(?, ?)";

        try (Connection connection =
                     dataSource.getConnection();

             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, projectId);
            statement.setString(2, status);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    tasks.add(
                            mapTask(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching project tasks by status",
                    e
            );
        }

        return tasks;
    }


    // =========================================================
    // BY DUE DATE
    // =========================================================

    public List<Task> findByDueDate(
            java.time.LocalDate dueDate) {

        List<Task> tasks =
                new ArrayList<>();

        String sql =
                "SELECT * FROM get_tasks_by_due_date(?)";

        try (Connection connection =
                     dataSource.getConnection();

             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setDate(
                    1,
                    Date.valueOf(dueDate)
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    tasks.add(
                            mapTask(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching tasks by due date",
                    e
            );
        }

        return tasks;
    }


    // =========================================================
    // BETWEEN DUE DATES
    // =========================================================

    public List<Task> findByDueDateBetween(
            java.time.LocalDate startDate,
            java.time.LocalDate endDate) {

        List<Task> tasks =
                new ArrayList<>();

        String sql =
                "SELECT * FROM get_tasks_between_due_dates(?, ?)";

        try (Connection connection =
                     dataSource.getConnection();

             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setDate(
                    1,
                    Date.valueOf(startDate)
            );

            statement.setDate(
                    2,
                    Date.valueOf(endDate)
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    tasks.add(
                            mapTask(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching tasks between due dates",
                    e
            );
        }

        return tasks;
    }


    // =========================================================
    // OVERDUE TASKS
    // =========================================================

    public List<Task>
    findByDueDateBeforeAndStatusNotIgnoreCase(
            java.time.LocalDate date,
            String status) {

        List<Task> tasks =
                new ArrayList<>();

        String sql =
                "SELECT * FROM get_overdue_tasks(?, ?)";

        try (Connection connection =
                     dataSource.getConnection();

             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setDate(
                    1,
                    Date.valueOf(date)
            );

            statement.setString(2, status);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    tasks.add(
                            mapTask(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error fetching overdue tasks",
                    e
            );
        }

        return tasks;
    }


    // =========================================================
    // SORT
    // =========================================================

    public List<Task> sortTasks(
            String field,
            String direction) {

        List<Task> tasks =
                new ArrayList<>();

        String sql =
                "SELECT * FROM sort_tasks(?, ?)";

        try (Connection connection =
                     dataSource.getConnection();

             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setString(1, field);
            statement.setString(2, direction);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    tasks.add(
                            mapTask(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error sorting tasks",
                    e
            );
        }

        return tasks;
    }


    // =========================================================
    // MAP RESULT SET TO TASK
    // =========================================================

    private Task mapTask(
            ResultSet rs) throws SQLException {

        Task task = new Task();

        task.setTaskId(
                rs.getInt("task_id")
        );

        task.setTaskName(
                rs.getString("task_name")
        );

        task.setDescription(
                rs.getString("description")
        );

        task.setPriority(
                rs.getString("priority")
        );

        task.setStatus(
                rs.getString("status")
        );


        // =====================================================
        // START DATE
        // =====================================================

        Date startDate =
                rs.getDate("start_date");

        if (startDate != null) {

            task.setStartDate(
                    startDate.toLocalDate()
            );
        }


        // =====================================================
        // DUE DATE
        // =====================================================

        Date dueDate =
                rs.getDate("due_date");

        if (dueDate != null) {

            task.setDueDate(
                    dueDate.toLocalDate()
            );
        }


        // =====================================================
        // PROJECT
        // =====================================================

        int projectId =
                rs.getInt("project_id");

        if (!rs.wasNull()) {

            Project project =
                    new Project();

            project.setProjectId(
                    projectId
            );

            /*
             * Only get project_name when the
             * ResultSet contains that column.
             *
             * get_tasks_by_employee() now returns it.
             * Other existing task functions may not.
             */

            try {

                String projectName =
                        rs.getString("project_name");

                project.setProjectName(
                        projectName
                );

            } catch (SQLException ignored) {

                // project_name not returned
                // by this particular function
            }

            task.setProject(project);
        }


        // =====================================================
        // EMPLOYEE
        // =====================================================

        int employeeId =
                rs.getInt("assigned_to");

        if (!rs.wasNull()) {

            Employee employee =
                    new Employee();

            employee.setEmployeeId(
                    employeeId
            );

            task.setAssignedTo(
                    employee
            );
        }

        return task;
    }
}