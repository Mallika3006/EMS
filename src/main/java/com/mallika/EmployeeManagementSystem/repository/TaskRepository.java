package com.mallika.EmployeeManagementSystem.repository;

import com.mallika.EmployeeManagementSystem.model.Employee;
import com.mallika.EmployeeManagementSystem.model.Project;
import com.mallika.EmployeeManagementSystem.model.Task;
import com.mallika.EmployeeManagementSystem.service.CompanyContext;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TaskRepository {
    private final DataSource dataSource;
    private final CompanyContext companyContext;

    public TaskRepository(DataSource dataSource, CompanyContext companyContext) {
        this.dataSource = dataSource;
        this.companyContext = companyContext;
    }

    public Task createTask(Task task, Long companyId) {
        String sql = "SELECT * FROM public.create_task(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Integer createdTaskId = null;
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, task.getTaskName());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getPriority());
            ps.setString(4, task.getStatus());
            setDate(ps, 5, task.getStartDate());
            setDate(ps, 6, task.getDueDate());
            setNullableInt(ps, 7, task.getProject() == null ? null : task.getProject().getProjectId());
            setNullableInt(ps, 8, task.getAssignedTo() == null ? null : task.getAssignedTo().getEmployeeId());
            ps.setLong(9, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    createdTaskId = rs.getInt("task_id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating task", e);
        }

        if (createdTaskId == null) {
            throw new RuntimeException("Task could not be created");
        }

        // The create_task function returns the raw tasks row. Reload through the
        // tenant-scoped query so project and assigned employee names are available
        // to mapTask(), and so the response follows the same shape as task records.
        return getTaskById(createdTaskId).orElseThrow(() ->
                new RuntimeException("Task was created but could not be reloaded: " + createdTaskId));
    }

    public List<Task> getAllTasks() {
        return queryTasks("SELECT t.*, p.project_name FROM public.tasks t LEFT JOIN public.projects p ON p.project_id=t.project_id AND p.company_id=t.company_id WHERE t.company_id=? ORDER BY t.task_id",
                ps -> ps.setLong(1, companyContext.getCompanyId()));
    }

    public Optional<Task> getTaskById(Integer id) {
        List<Task> found = queryTasks("SELECT t.*, p.project_name FROM public.tasks t LEFT JOIN public.projects p ON p.project_id=t.project_id AND p.company_id=t.company_id WHERE t.task_id=? AND t.company_id=?",
                ps -> { ps.setInt(1, id); ps.setLong(2, companyContext.getCompanyId()); });
        return found.stream().findFirst();
    }

    public Task updateTask(Integer id, Task details, Long companyId) {
        String sql = "UPDATE public.tasks t SET task_name=?, description=?, priority=?, status=?, start_date=?, due_date=?, project_id=?, assigned_to=? WHERE task_id=? AND company_id=? AND (project_id IS NULL OR EXISTS (SELECT 1 FROM public.projects p WHERE p.project_id=? AND p.company_id=?)) AND (assigned_to IS NULL OR EXISTS (SELECT 1 FROM public.employees e WHERE e.employee_id=? AND e.company_id=?))";
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, details.getTaskName());
            ps.setString(2, details.getDescription());
            ps.setString(3, details.getPriority());
            ps.setString(4, details.getStatus());
            setDate(ps, 5, details.getStartDate());
            setDate(ps, 6, details.getDueDate());
            Integer projectId = details.getProject() == null ? null : details.getProject().getProjectId();
            Integer employeeId = details.getAssignedTo() == null ? null : details.getAssignedTo().getEmployeeId();
            setNullableInt(ps, 7, projectId);
            setNullableInt(ps, 8, employeeId);
            ps.setInt(9, id);
            ps.setLong(10, companyId);
            setNullableInt(ps, 11, projectId);
            ps.setLong(12, companyId);
            setNullableInt(ps, 13, employeeId);
            ps.setLong(14, companyId);

            int updatedRows = ps.executeUpdate();
            if (updatedRows == 0) {
                throw new RuntimeException(
                        "Task not found in current company or project/employee belongs to another company: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating task with id: " + id, e);
        }

        // Reload through the tenant-scoped query, which includes project and employee names.
        return getTaskById(id).orElseThrow(() ->
                new RuntimeException("Task updated but could not be reloaded: " + id));
    }

    public Task updateTaskStatus(Integer id, String status, Long companyId) {
        String sql = "UPDATE public.tasks SET status=? WHERE task_id=? AND company_id=?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.setLong(3, companyId);
            if (ps.executeUpdate() == 0) {
                throw new RuntimeException("Task not found in current company: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating status for task id: " + id, e);
        }

        return getTaskById(id).orElseThrow(() ->
                new RuntimeException("Task status updated but task could not be reloaded: " + id));
    }

    public boolean deleteTask(Integer id, Long companyId) {
        String sql = "DELETE FROM public.tasks WHERE task_id=? AND company_id=?";
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setLong(2, companyId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting task with id: " + id, e);
        }
    }

    public List<Task> searchByTaskName(String taskName) {
        return queryTasks("SELECT t.*, p.project_name FROM public.tasks t LEFT JOIN public.projects p ON p.project_id=t.project_id AND p.company_id=t.company_id WHERE t.company_id=? AND t.task_name ILIKE ? ORDER BY t.task_id",
                ps -> { ps.setLong(1, companyContext.getCompanyId()); ps.setString(2, "%" + taskName + "%"); });
    }

    public List<Task> findByProjectProjectId(Integer projectId) {
        return queryTasks("SELECT t.*, p.project_name FROM public.tasks t LEFT JOIN public.projects p ON p.project_id=t.project_id AND p.company_id=t.company_id WHERE t.project_id=? AND t.company_id=? ORDER BY t.task_id",
                ps -> { ps.setInt(1, projectId); ps.setLong(2, companyContext.getCompanyId()); });
    }

    public List<Task> findByAssignedToEmployeeId(Integer employeeId) {
        return queryTasks("SELECT t.*, p.project_name FROM public.tasks t LEFT JOIN public.projects p ON p.project_id=t.project_id AND p.company_id=t.company_id WHERE t.assigned_to=? AND t.company_id=? AND EXISTS (SELECT 1 FROM public.employees e WHERE e.employee_id=? AND e.company_id=?) ORDER BY t.task_id",
                ps -> { long companyId = companyContext.getCompanyId(); ps.setInt(1, employeeId); ps.setLong(2, companyId); ps.setInt(3, employeeId); ps.setLong(4, companyId); });
    }

    public List<Task> findByStatusIgnoreCase(String status) {
        return queryTasks("SELECT t.*, p.project_name FROM public.tasks t LEFT JOIN public.projects p ON p.project_id=t.project_id AND p.company_id=t.company_id WHERE t.company_id=? AND LOWER(t.status)=LOWER(?) ORDER BY t.task_id",
                ps -> { ps.setLong(1, companyContext.getCompanyId()); ps.setString(2, status); });
    }

    public List<Task> findByPriorityIgnoreCase(String priority) {
        return queryTasks("SELECT t.*, p.project_name FROM public.tasks t LEFT JOIN public.projects p ON p.project_id=t.project_id AND p.company_id=t.company_id WHERE t.company_id=? AND LOWER(t.priority)=LOWER(?) ORDER BY t.task_id",
                ps -> { ps.setLong(1, companyContext.getCompanyId()); ps.setString(2, priority); });
    }

    public List<Task> findByAssignedToEmployeeIdAndStatusIgnoreCase(Integer employeeId, String status) {
        return queryTasks("SELECT t.*, p.project_name FROM public.tasks t LEFT JOIN public.projects p ON p.project_id=t.project_id AND p.company_id=t.company_id WHERE t.assigned_to=? AND t.company_id=? AND LOWER(t.status)=LOWER(?) AND EXISTS (SELECT 1 FROM public.employees e WHERE e.employee_id=? AND e.company_id=?) ORDER BY t.task_id",
                ps -> { long companyId = companyContext.getCompanyId(); ps.setInt(1, employeeId); ps.setLong(2, companyId); ps.setString(3, status); ps.setInt(4, employeeId); ps.setLong(5, companyId); });
    }

    public List<Task> findByProjectProjectIdAndStatusIgnoreCase(Integer projectId, String status) {
        return queryTasks("SELECT t.*, p.project_name FROM public.tasks t LEFT JOIN public.projects p ON p.project_id=t.project_id AND p.company_id=t.company_id WHERE t.project_id=? AND t.company_id=? AND LOWER(t.status)=LOWER(?) AND EXISTS (SELECT 1 FROM public.projects x WHERE x.project_id=? AND x.company_id=?) ORDER BY t.task_id",
                ps -> { long companyId = companyContext.getCompanyId(); ps.setInt(1, projectId); ps.setLong(2, companyId); ps.setString(3, status); ps.setInt(4, projectId); ps.setLong(5, companyId); });
    }

    public List<Task> findByDueDate(LocalDate dueDate) {
        return queryTasks("SELECT t.*, p.project_name FROM public.tasks t LEFT JOIN public.projects p ON p.project_id=t.project_id AND p.company_id=t.company_id WHERE t.company_id=? AND t.due_date=? ORDER BY t.task_id",
                ps -> { ps.setLong(1, companyContext.getCompanyId()); ps.setDate(2, Date.valueOf(dueDate)); });
    }

    public List<Task> findByDueDateBetween(LocalDate startDate, LocalDate endDate) {
        return queryTasks("SELECT t.*, p.project_name FROM public.tasks t LEFT JOIN public.projects p ON p.project_id=t.project_id AND p.company_id=t.company_id WHERE t.company_id=? AND t.due_date BETWEEN ? AND ? ORDER BY t.due_date, t.task_id",
                ps -> { ps.setLong(1, companyContext.getCompanyId()); ps.setDate(2, Date.valueOf(startDate)); ps.setDate(3, Date.valueOf(endDate)); });
    }

    public List<Task> findByDueDateBeforeAndStatusNotIgnoreCase(LocalDate date, String status) {
        return queryTasks("SELECT t.*, p.project_name FROM public.tasks t LEFT JOIN public.projects p ON p.project_id=t.project_id AND p.company_id=t.company_id WHERE t.company_id=? AND t.due_date<? AND LOWER(t.status)<>LOWER(?) ORDER BY t.due_date, t.task_id",
                ps -> { ps.setLong(1, companyContext.getCompanyId()); ps.setDate(2, Date.valueOf(date)); ps.setString(3, status); });
    }

    public List<Task> sortTasks(String field, String direction) {
        String column = switch (field == null ? "" : field.toLowerCase()) {
            case "task_id", "id" -> "task_id";
            case "task_name", "name" -> "task_name";
            case "priority" -> "priority";
            case "status" -> "status";
            case "start_date" -> "start_date";
            case "due_date" -> "due_date";
            default -> throw new IllegalArgumentException("Unsupported task sort field: " + field);
        };
        String order = "desc".equalsIgnoreCase(direction) ? "DESC" : "ASC";
        return queryTasks("SELECT t.*, p.project_name FROM public.tasks t LEFT JOIN public.projects p ON p.project_id=t.project_id AND p.company_id=t.company_id WHERE t.company_id=? ORDER BY t." + column + " " + order,
                ps -> ps.setLong(1, companyContext.getCompanyId()));
    }

    public List<Task> getTasksByManagerId(Integer managerId) {
        return queryTasks("SELECT t.*, p.project_name FROM public.tasks t LEFT JOIN public.projects p ON p.project_id=t.project_id AND p.company_id=t.company_id LEFT JOIN public.employees e ON e.employee_id=t.assigned_to AND e.company_id=t.company_id LEFT JOIN public.users u ON u.employee_id=e.employee_id AND u.company_id=e.company_id WHERE t.company_id=? AND (e.employee_id IS NULL OR u.is_active=TRUE) AND (e.manager_id=? OR p.manager_id=?) ORDER BY t.task_id DESC",
                ps -> { long companyId = companyContext.getCompanyId(); ps.setLong(1, companyId); ps.setInt(2, managerId); ps.setInt(3, managerId); });
    }

    public boolean isTaskAccessibleToManager(Integer taskId, Integer managerId, Long companyId) {
        String sql = """
            SELECT 1
            FROM public.tasks t
            LEFT JOIN public.projects p
              ON p.project_id = t.project_id AND p.company_id = t.company_id
            WHERE t.task_id = ? AND t.company_id = ?
              AND (
                p.manager_id = ?
                OR EXISTS (
                    SELECT 1 FROM public.employees e
                    WHERE e.employee_id = t.assigned_to
                      AND e.company_id = t.company_id
                      AND e.manager_id = ?
                )
              )
            """;
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ps.setLong(2, companyId);
            ps.setInt(3, managerId);
            ps.setInt(4, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not validate manager task access", e);
        }
    }

    public boolean isEmployeeManagedBy(Integer employeeId, Integer managerId, Long companyId) {
        String sql = """
            SELECT 1 FROM public.employees
            WHERE employee_id = ? AND manager_id = ? AND company_id = ?
            """;
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, managerId);
            ps.setLong(3, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not validate manager employee relationship", e);
        }
    }

    public boolean isProjectManagedBy(Integer projectId, Integer managerId, Long companyId) {
        String sql = """
            SELECT 1 FROM public.projects
            WHERE project_id = ? AND manager_id = ? AND company_id = ?
            """;
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            ps.setInt(2, managerId);
            ps.setLong(3, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not validate manager project relationship", e);
        }
    }

    private List<Task> queryTasks(String sql, SqlBinder binder) {
        List<Task> tasks = new ArrayList<>();
        String enrichedSql = sql;
        String selectPrefix = "SELECT t.*, p.project_name FROM public.tasks t LEFT JOIN public.projects p ON p.project_id=t.project_id AND p.company_id=t.company_id";
        String selectWithNames = "SELECT t.*, p.project_name, e.first_name AS assigned_first_name, e.last_name AS assigned_last_name FROM public.tasks t LEFT JOIN public.projects p ON p.project_id=t.project_id AND p.company_id=t.company_id";
        if (sql.contains("LEFT JOIN public.employees e")) {
            enrichedSql = sql.replace("SELECT t.*, p.project_name FROM public.tasks t", "SELECT t.*, p.project_name, e.first_name AS assigned_first_name, e.last_name AS assigned_last_name FROM public.tasks t");
        } else if (sql.startsWith(selectPrefix)) {
            enrichedSql = sql.replace(selectPrefix, selectWithNames + " LEFT JOIN public.employees e ON e.employee_id=t.assigned_to AND e.company_id=t.company_id");
        }
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(enrichedSql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) tasks.add(mapTask(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error querying tasks for current company", e);
        }
        return tasks;
    }

    private void setDate(PreparedStatement ps, int index, LocalDate value) throws SQLException {
        if (value == null) ps.setNull(index, Types.DATE);
        else ps.setDate(index, Date.valueOf(value));
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) ps.setNull(index, Types.INTEGER);
        else ps.setInt(index, value);
    }

    private Task mapTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setTaskId(rs.getInt("task_id"));
        task.setTaskName(rs.getString("task_name"));
        task.setDescription(rs.getString("description"));
        task.setPriority(rs.getString("priority"));
        task.setStatus(rs.getString("status"));
        Date startDate = rs.getDate("start_date");
        if (startDate != null) task.setStartDate(startDate.toLocalDate());
        Date dueDate = rs.getDate("due_date");
        if (dueDate != null) task.setDueDate(dueDate.toLocalDate());

        int projectId = rs.getInt("project_id");
        if (!rs.wasNull()) {
            Project project = new Project();
            project.setProjectId(projectId);
            try { project.setProjectName(rs.getString("project_name")); }
            catch (SQLException ignored) { }
            task.setProject(project);
        }

        int employeeId = rs.getInt("assigned_to");
        if (!rs.wasNull()) {
            Employee employee = new Employee();
            employee.setEmployeeId(employeeId);
            employee.setFirstName(rs.getString("assigned_first_name"));
            employee.setLastName(rs.getString("assigned_last_name"));
            task.setAssignedTo(employee);
        }
        return task;
    }

    @FunctionalInterface
    private interface SqlBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }
}
