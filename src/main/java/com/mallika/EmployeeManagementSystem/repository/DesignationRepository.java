
package com.mallika.EmployeeManagementSystem.repository;

import com.mallika.EmployeeManagementSystem.model.Designation;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class DesignationRepository {

    private final DataSource dataSource;

    public DesignationRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // CREATE
    public Designation createDesignation(
            Designation designation, Long companyId) {

        String sql = "SELECT * FROM create_designation(?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, designation.getDesignationTitle());
            statement.setBigDecimal(2, designation.getMinSalary());
            statement.setBigDecimal(3, designation.getMaxSalary());
            statement.setLong(4, companyId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapDesignation(rs, companyId);
                }
            }

            throw new RuntimeException("Failed to create designation");

        } catch (SQLException e) {
            throw new RuntimeException("Error creating designation", e);
        }
    }

    // GET ALL
    public List<Designation> getAllDesignations(Long companyId) {

        String sql = "SELECT * FROM get_all_designations(?)";
        List<Designation> designations = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, companyId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    designations.add(mapDesignation(rs, companyId));
                }
            }

            return designations;

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching designations", e);
        }
    }

    // GET BY ID
    public Optional<Designation> getDesignationById(
            Integer id, Long companyId) {

        String sql = "SELECT * FROM get_designation_by_id(?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.setLong(2, companyId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapDesignation(rs, companyId));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching designation by ID", e);
        }
    }

    // UPDATE
    public Designation updateDesignation(
            Integer id, Designation designation, Long companyId) {

        String sql = "SELECT * FROM update_designation(?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.setString(2, designation.getDesignationTitle());
            statement.setBigDecimal(3, designation.getMinSalary());
            statement.setBigDecimal(4, designation.getMaxSalary());
            statement.setLong(5, companyId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapDesignation(rs, companyId);
                }
            }

            throw new RuntimeException(
                    "Designation not found with ID: " + id);

        } catch (SQLException e) {
            throw new RuntimeException("Error updating designation", e);
        }
    }

    // DELETE
    public void deleteDesignation(Integer id, Long companyId) {

        String sql = "SELECT delete_designation(?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.setLong(2, companyId);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next() || !rs.getBoolean(1)) {
                    throw new RuntimeException(
                            "Designation not found with ID: " + id);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting designation", e);
        }
    }

    // SEARCH BY TITLE
    public List<Designation> searchByTitle(
            String title, Long companyId) {

        String sql = "SELECT * FROM search_designations_by_title(?, ?)";
        List<Designation> designations = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, title);
            statement.setLong(2, companyId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    designations.add(mapDesignation(rs, companyId));
                }
            }

            return designations;

        } catch (SQLException e) {
            throw new RuntimeException("Error searching designations", e);
        }
    }

    // GET BY EXACT TITLE
    public Optional<Designation> getDesignationByTitle(
            String title, Long companyId) {

        String sql = "SELECT * FROM get_designation_by_title(?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, title);
            statement.setLong(2, companyId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapDesignation(rs, companyId));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching designation by title", e);
        }
    }

    // MAPPER
    private Designation mapDesignation(
            ResultSet rs, Long companyId) throws SQLException {

        Designation designation = new Designation();

        designation.setDesignationId(rs.getInt("designation_id"));
        designation.setDesignationTitle(rs.getString("designation_title"));

        BigDecimal minSalary = rs.getBigDecimal("min_salary");
        BigDecimal maxSalary = rs.getBigDecimal("max_salary");

        designation.setMinSalary(minSalary);
        designation.setMaxSalary(maxSalary);
        designation.setCompanyId(companyId);

        return designation;
    }
}