package com.mallika.EmployeeManagementSystem.repository;

import com.mallika.EmployeeManagementSystem.model.Project;
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
public class ProjectRepository {

    private final DataSource dataSource;

    public ProjectRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // CREATE
    public Project createProject(Project project) {

        String sql = "SELECT * FROM create_project(?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setString(1, project.getProjectName());

            if (project.getStartDate() != null) {
                statement.setDate(2, Date.valueOf(project.getStartDate()));
            } else {
                statement.setNull(2, java.sql.Types.DATE);
            }

            if (project.getEndDate() != null) {
                statement.setDate(3, Date.valueOf(project.getEndDate()));
            } else {
                statement.setNull(3, java.sql.Types.DATE);
            }

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapProject(resultSet);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error creating project", e);
        }

        throw new RuntimeException("Project could not be created");
    }

    // GET ALL
    public List<Project> getAllProjects() {

        List<Project> projects = new ArrayList<>();

        String sql = "SELECT * FROM get_all_projects()";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                projects.add(mapProject(resultSet));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching projects", e);
        }

        return projects;
    }

    // GET BY ID
    public Optional<Project> getProjectById(Integer id) {

        String sql = "SELECT * FROM get_project_by_id(?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(mapProject(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching project with id: " + id,
                    e
            );
        }

        return Optional.empty();
    }

    // UPDATE
    public Project updateProject(
            Integer id,
            Project projectDetails) {

        String sql = "SELECT * FROM update_project(?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setInt(1, id);
            statement.setString(2, projectDetails.getProjectName());

            if (projectDetails.getStartDate() != null) {
                statement.setDate(
                        3,
                        Date.valueOf(projectDetails.getStartDate())
                );
            } else {
                statement.setNull(3, java.sql.Types.DATE);
            }

            if (projectDetails.getEndDate() != null) {
                statement.setDate(
                        4,
                        Date.valueOf(projectDetails.getEndDate())
                );
            } else {
                statement.setNull(4, java.sql.Types.DATE);
            }

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapProject(resultSet);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error updating project with id: " + id,
                    e
            );
        }

        throw new RuntimeException(
                "Project not found with id: " + id
        );
    }

    // DELETE
    public boolean deleteProject(Integer id) {

        String sql = "SELECT delete_project(?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getBoolean(1);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error deleting project with id: " + id,
                    e
            );
        }

        return false;
    }

    // SEARCH BY NAME
    public List<Project> searchByProjectName(String projectName) {

        List<Project> projects = new ArrayList<>();

        String sql = "SELECT * FROM search_projects_by_name(?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setString(1, projectName);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    projects.add(mapProject(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error searching projects by name",
                    e
            );
        }

        return projects;
    }

    // GET BY EXACT NAME
    public List<Project> getProjectsByExactName(String projectName) {

        List<Project> projects = new ArrayList<>();

        String sql = "SELECT * FROM get_projects_by_exact_name(?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setString(1, projectName);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    projects.add(mapProject(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching projects by exact name",
                    e
            );
        }

        return projects;
    }

    // GET BY START DATE
    public List<Project> getByStartDate(java.time.LocalDate startDate) {

        List<Project> projects = new ArrayList<>();

        String sql = "SELECT * FROM get_projects_by_start_date(?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setDate(1, Date.valueOf(startDate));

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    projects.add(mapProject(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching projects by start date",
                    e
            );
        }

        return projects;
    }

    // GET BY END DATE
    public List<Project> getByEndDate(java.time.LocalDate endDate) {

        List<Project> projects = new ArrayList<>();

        String sql = "SELECT * FROM get_projects_by_end_date(?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setDate(1, Date.valueOf(endDate));

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    projects.add(mapProject(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching projects by end date",
                    e
            );
        }

        return projects;
    }

    // GET PROJECTS BETWEEN DATES
    public List<Project> getProjectsBetweenDates(
            java.time.LocalDate startDate,
            java.time.LocalDate endDate) {

        List<Project> projects = new ArrayList<>();

        String sql = "SELECT * FROM get_projects_between_dates(?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setDate(1, Date.valueOf(startDate));
            statement.setDate(2, Date.valueOf(endDate));

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    projects.add(mapProject(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching projects between dates",
                    e
            );
        }

        return projects;
    }

    // GET ONGOING PROJECTS
    public List<Project> getOngoingProjects() {

        List<Project> projects = new ArrayList<>();

        String sql = "SELECT * FROM get_ongoing_projects()";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                projects.add(mapProject(resultSet));
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching ongoing projects",
                    e
            );
        }

        return projects;
    }

    // SORT
    public List<Project> sortProjects(
            String field,
            String direction) {

        List<Project> projects = new ArrayList<>();

        String sql = "SELECT * FROM sort_projects(?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setString(1, field);
            statement.setString(2, direction);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    projects.add(mapProject(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error sorting projects",
                    e
            );
        }

        return projects;
    }

    // MAP RESULT SET TO PROJECT
    private Project mapProject(ResultSet rs) throws SQLException {

        Project project = new Project();

        project.setProjectId(
                rs.getInt("project_id")
        );

        project.setProjectName(
                rs.getString("project_name")
        );

        Date startDate = rs.getDate("start_date");

        if (startDate != null) {
            project.setStartDate(
                    startDate.toLocalDate()
            );
        }

        Date endDate = rs.getDate("end_date");

        if (endDate != null) {
            project.setEndDate(
                    endDate.toLocalDate()
            );
        }

        try {
            project.setManagerId(
                    rs.getObject("manager_id", Integer.class)
            );
        } catch (SQLException e) {
            project.setManagerId(null);
        }

        return project;
    }

    // GET PROJECTS BY EMPLOYEE
    public List<Project> getProjectsByEmployeeId(Integer employeeId) {

        List<Project> projects = new ArrayList<>();

        String sql = "SELECT * FROM get_projects_by_employee(?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setInt(1, employeeId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    projects.add(mapProject(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching projects for employee: "
                            + employeeId,
                    e
            );
        }

        return projects;
    }

    // GET PROJECTS BY MANAGER
    public List<Project> getProjectsByManagerId(Integer managerId) {

        List<Project> projects = new ArrayList<>();

        String sql = "SELECT * FROM get_projects_by_manager(?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setInt(1, managerId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    projects.add(mapProject(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching projects for manager: "
                            + managerId,
                    e
            );
        }

        return projects;
    }

    // GET UNASSIGNED PROJECTS BY MANAGER
    public List<Project> getUnassignedProjectsByManagerId(
            Integer managerId) {

        List<Project> projects = new ArrayList<>();

        String sql =
                "SELECT * FROM get_unassigned_projects_by_manager(?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setInt(1, managerId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    projects.add(mapProject(resultSet));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching unassigned projects for manager: "
                            + managerId,
                    e
            );
        }

        return projects;
    }
}