package com.mallika.EmployeeManagementSystem.repository;

import com.mallika.EmployeeManagementSystem.model.Project;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
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
        String sql = "SELECT * FROM create_project(?, ?, ?, ?, ?)";

        try (Connection con = dataSource.getConnection();
             CallableStatement st = con.prepareCall(sql)) {

            st.setString(1, project.getProjectName());
            setDate(st, 2, project.getStartDate());
            setDate(st, 3, project.getEndDate());

            if (project.getManagerId() != null) {
                st.setInt(4, project.getManagerId());
            } else {
                st.setNull(4, Types.INTEGER);
            }

            st.setLong(5, companyId);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapProject(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating project", e);
        }

        throw new RuntimeException("Project could not be created");
    }

    public List<Project> getAllProjects(Long companyId) {
        String sql = "SELECT * FROM get_all_projects(?)";
        List<Project> projects = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             CallableStatement st = con.prepareCall(sql)) {

            st.setLong(1, companyId);

            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    projects.add(mapProject(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching projects", e);
        }

        return projects;
    }

    public Optional<Project> getProjectById(
            Integer id, Long companyId) {

        String sql = "SELECT * FROM get_project_by_id(?, ?)";

        try (Connection con = dataSource.getConnection();
             CallableStatement st = con.prepareCall(sql)) {

            st.setInt(1, id);
            st.setLong(2, companyId);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapProject(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching project: " + id, e);
        }

        return Optional.empty();
    }

    public Project updateProject(
            Integer id, Project details, Long companyId) {

        String sql = "SELECT * FROM update_project(?, ?, ?, ?, ?)";

        try (Connection con = dataSource.getConnection();
             CallableStatement st = con.prepareCall(sql)) {

            st.setInt(1, id);
            st.setString(2, details.getProjectName());
            setDate(st, 3, details.getStartDate());
            setDate(st, 4, details.getEndDate());
            st.setLong(5, companyId);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapProject(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error updating project: " + id, e);
        }

        throw new RuntimeException(
                "Project not found with id: " + id);
    }

    public boolean deleteProject(Integer id, Long companyId) {
        String sql = "SELECT delete_project(?, ?)";

        try (Connection con = dataSource.getConnection();
             CallableStatement st = con.prepareCall(sql)) {

            st.setInt(1, id);
            st.setLong(2, companyId);

            try (ResultSet rs = st.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error deleting project: " + id, e);
        }
    }

    private void setDate(
            CallableStatement st, int index,
            java.time.LocalDate date) throws SQLException {

        if (date != null) {
            st.setDate(index, Date.valueOf(date));
        } else {
            st.setNull(index, Types.DATE);
        }
    }

    private Project mapProject(ResultSet rs) throws SQLException {
        Project project = new Project();

        project.setProjectId(rs.getInt("project_id"));
        project.setProjectName(rs.getString("project_name"));

        Date startDate = rs.getDate("start_date");
        if (startDate != null) {
            project.setStartDate(startDate.toLocalDate());
        }

        Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            project.setEndDate(endDate.toLocalDate());
        }

        project.setManagerId(
                rs.getObject("manager_id", Integer.class));

        return project;
    }

    public List<Project> getProjectsByManager(Integer managerId, Long companyId) {
        String sql = "SELECT * FROM get_projects_by_manager(?, ?)";
        List<Project> projects = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             CallableStatement st = con.prepareCall(sql)) {

            st.setInt(1, managerId);
            st.setLong(2, companyId);

            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    Project project = new Project();

                    project.setProjectId(rs.getInt("project_id"));
                    project.setProjectName(rs.getString("project_name"));

                    java.sql.Date startDate = rs.getDate("start_date");
                    if (startDate != null) {
                        project.setStartDate(startDate.toLocalDate());
                    }

                    java.sql.Date endDate = rs.getDate("end_date");
                    if (endDate != null) {
                        project.setEndDate(endDate.toLocalDate());
                    }

                    int assignedManagerId = rs.getInt("manager_id");
                    project.setManagerId(
                            rs.wasNull() ? null : assignedManagerId);

                    projects.add(project);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching manager projects", e);
        }

        return projects;
    }

    public List<Project> getProjectsByEmployee(
            Integer employeeId, Long companyId) {

        String sql = "SELECT * FROM get_projects_by_employee(?, ?)";
        List<Project> projects = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             CallableStatement st = con.prepareCall(sql)) {

            st.setInt(1, employeeId);
            st.setLong(2, companyId);

            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    projects.add(mapProject(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching employee projects", e);
        }

        return projects;
    }

}