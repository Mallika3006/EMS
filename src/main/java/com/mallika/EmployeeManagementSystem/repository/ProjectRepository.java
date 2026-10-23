package com.mallika.EmployeeManagementSystem.repository;

import com.mallika.EmployeeManagementSystem.model.Project;
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
public class ProjectRepository {
    private final DataSource dataSource;

    public ProjectRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Project createProject(Project project, Long companyId) {
        if (project.getManagerId() != null) {
            validateEmployeeCompany(project.getManagerId(), companyId);
        }
        String sql = """
            INSERT INTO public.projects
                (project_name, start_date, end_date, manager_id, company_id)
            VALUES (?, ?, ?, ?, ?)
            RETURNING project_id, project_name, start_date, end_date, manager_id
            """;
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, project.getProjectName());
            setDate(ps, 2, project.getStartDate());
            setDate(ps, 3, project.getEndDate());
            setNullableInt(ps, 4, project.getManagerId());
            ps.setLong(5, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapProject(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating project", e);
        }
        throw new RuntimeException("Project could not be created");
    }

    public List<Project> getAllProjects(Long companyId) {
        return queryProjects("""
            SELECT project_id, project_name, start_date, end_date, manager_id
            FROM public.projects WHERE company_id = ? ORDER BY project_id
            """, ps -> ps.setLong(1, companyId));
    }

    public Optional<Project> getProjectById(Integer id, Long companyId) {
        return queryOne("""
            SELECT project_id, project_name, start_date, end_date, manager_id
            FROM public.projects WHERE project_id = ? AND company_id = ?
            """, ps -> { ps.setInt(1, id); ps.setLong(2, companyId); });
    }

    public Project updateProject(Integer id, Project details, Long companyId) {
        String sql = """
            UPDATE public.projects
            SET project_name = ?, start_date = ?, end_date = ?
            WHERE project_id = ? AND company_id = ?
            RETURNING project_id, project_name, start_date, end_date, manager_id
            """;
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, details.getProjectName());
            setDate(ps, 2, details.getStartDate());
            setDate(ps, 3, details.getEndDate());
            ps.setInt(4, id);
            ps.setLong(5, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapProject(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating project: " + id, e);
        }
        throw new RuntimeException("Project not found in current company: " + id);
    }

    public boolean deleteProject(Integer id, Long companyId) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM public.projects WHERE project_id = ? AND company_id = ?")) {
            ps.setInt(1, id);
            ps.setLong(2, companyId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting project: " + id, e);
        }
    }

    public List<Project> getProjectsByManager(Integer managerId, Long companyId) {
        return queryProjects("""
            SELECT project_id, project_name, start_date, end_date, manager_id
            FROM public.projects
            WHERE manager_id = ? AND company_id = ?
            ORDER BY project_id
            """, ps -> { ps.setInt(1, managerId); ps.setLong(2, companyId); });
    }

    public List<Project> getProjectsVisibleToManager(Integer managerId, Long companyId) {
        return queryProjects("""
            SELECT project_id, project_name, start_date, end_date, manager_id
            FROM public.projects
            WHERE company_id = ? AND (manager_id = ? OR manager_id IS NULL)
            ORDER BY project_id
            """, ps -> { ps.setLong(1, companyId); ps.setInt(2, managerId); });
    }

    public List<Project> getProjectsByEmployee(Integer employeeId, Long companyId) {
        return queryProjects("""
            SELECT DISTINCT p.project_id, p.project_name, p.start_date, p.end_date, p.manager_id
            FROM public.projects p
            JOIN public.employee_project ep ON ep.project_id = p.project_id
            JOIN public.employees e ON e.employee_id = ep.employee_id
            WHERE ep.employee_id = ?
              AND p.company_id = ?
              AND e.company_id = ?
            ORDER BY p.project_id
            """, ps -> { ps.setInt(1, employeeId); ps.setLong(2, companyId); ps.setLong(3, companyId); });
    }

    public boolean assignProjectToEmployee(
            Integer projectId, Integer employeeId,
            Integer managerId, Long companyId) {
        String insertSql = """
            INSERT INTO public.employee_project (employee_id, project_id, company_id)
            SELECT e.employee_id, p.project_id, p.company_id
            FROM public.employees e
            JOIN public.users u
              ON u.employee_id = e.employee_id
             AND u.company_id = e.company_id
             AND u.is_active = TRUE
            JOIN public.projects p
              ON p.company_id = e.company_id
            WHERE e.employee_id = ?
              AND e.manager_id = ?
              AND e.company_id = ?
              AND p.project_id = ?
              AND p.manager_id = ?
              AND NOT EXISTS (
                  SELECT 1 FROM public.employee_project ep
                  WHERE ep.employee_id = e.employee_id
                    AND ep.project_id = p.project_id
              )
            RETURNING employee_id
            """;
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(insertSql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, managerId);
            ps.setLong(3, companyId);
            ps.setInt(4, projectId);
            ps.setInt(5, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error assigning project to employee", e);
        }

        String existingSql = """
            SELECT 1
            FROM public.employee_project ep
            JOIN public.projects p
              ON p.project_id = ep.project_id AND p.company_id = ep.company_id
            JOIN public.employees e
              ON e.employee_id = ep.employee_id AND e.company_id = ep.company_id
            WHERE ep.project_id = ? AND ep.employee_id = ?
              AND ep.company_id = ? AND p.manager_id = ? AND e.manager_id = ?
            """;
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(existingSql)) {
            ps.setInt(1, projectId);
            ps.setInt(2, employeeId);
            ps.setLong(3, companyId);
            ps.setInt(4, managerId);
            ps.setInt(5, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking project assignment", e);
        }
    }

    private void validateEmployeeCompany(Integer employeeId, Long companyId) {
        String sql = """
            SELECT 1
            FROM public.employees e
            JOIN public.users u
              ON u.employee_id = e.employee_id
             AND u.company_id = e.company_id
            JOIN public.roles r ON r.role_id = u.role_id
            WHERE e.employee_id = ?
              AND e.company_id = ?
              AND u.is_active = TRUE
              AND LOWER(r.role_name) = 'manager'
            """;
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setLong(2, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "Project manager must be an active Manager in this company");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not validate project manager company", e);
        }
    }

    private List<Project> queryProjects(String sql, Binder binder) {
        List<Project> projects = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) projects.add(mapProject(rs));
            }
            return projects;
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching projects for company", e);
        }
    }

    private Optional<Project> queryOne(String sql, Binder binder) {
        return queryProjects(sql, binder).stream().findFirst();
    }

    private Project mapProject(ResultSet rs) throws SQLException {
        Project project = new Project();
        project.setProjectId(rs.getInt("project_id"));
        project.setProjectName(rs.getString("project_name"));
        Date startDate = rs.getDate("start_date");
        if (startDate != null) project.setStartDate(startDate.toLocalDate());
        Date endDate = rs.getDate("end_date");
        if (endDate != null) project.setEndDate(endDate.toLocalDate());
        project.setManagerId(rs.getObject("manager_id", Integer.class));
        return project;
    }

    private void setDate(PreparedStatement ps, int index, LocalDate value) throws SQLException {
        if (value == null) ps.setNull(index, Types.DATE);
        else ps.setDate(index, Date.valueOf(value));
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) ps.setNull(index, Types.INTEGER);
        else ps.setInt(index, value);
    }

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }
}