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
                if (rs.next()) return mapTask(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating task", e);
        }
        throw new RuntimeException("Task could not be created");
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
        String sql = "UPDATE public.tasks t SET task_name=?, description=?, priority=?, status=?, start_date=?, due_date=?, project_id=?, assigned_to=? WHERE task_id=? AND company_id=? AND (project_id IS NULL OR EXISTS (SELECT 1 FROM public.projects p WHERE p.project_id=? AND p.company_id=?)) AND (assigned_to IS NULL OR EXISTS (SELECT 1 FROM public.employees e WHERE e.employee_id=? AND e.company_id=?)) RETURNING t.*";
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
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapTask(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating task with id: " + id, e);
        }
        throw new RuntimeException("Task not found in current company or project/employee belongs to another company: " + id);
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
        return queryTasks("SELECT t.*, p.project_name FROM public.tasks t LEFT JOIN public.projects p ON p.project_id=t.project_id AND p.company_id=t.company_id JOIN public.employees e ON e.employee_id=t.assigned_to AND e.company_id=t.company_id WHERE e.manager_id=? AND t.company_id=? ORDER BY t.task_id DESC",
                ps -> { ps.setInt(1, managerId); ps.setLong(2, companyContext.getCompanyId()); });
    }

    private List<Task> queryTasks(String sql, SqlBinder binder) {
        List<Task> tasks = new ArrayList<>();
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
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
            task.setAssignedTo(employee);
        }
        return task;
    }

    @FunctionalInterface
    private interface SqlBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }
}
