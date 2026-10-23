
package com.mallika.EmployeeManagementSystem.service;

import com.mallika.EmployeeManagementSystem.exception.ResourceNotFoundException;
import com.mallika.EmployeeManagementSystem.model.Designation;
import com.mallika.EmployeeManagementSystem.model.User;
import com.mallika.EmployeeManagementSystem.repository.DesignationRepository;
import com.mallika.EmployeeManagementSystem.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DesignationService {

    private final DesignationRepository designationRepository;
    private final UserRepository userRepository;

    public DesignationService(
            DesignationRepository designationRepository,
            UserRepository userRepository) {
        this.designationRepository = designationRepository;
        this.userRepository = userRepository;
    }

    // Get company ID of the authenticated user
    private Long getCurrentCompanyId() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            throw new SecurityException("User is not authenticated");
        }

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"));

        if (user.getCompanyId() == null) {
            throw new SecurityException(
                    "No company is assigned to the authenticated user");
        }

        return user.getCompanyId();
    }

    private void validateDesignation(
            Designation designation, Integer currentId, Long companyId) {
        if (designation == null || designation.getDesignationTitle() == null
                || designation.getDesignationTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Designation title is required.");
        }
        String title = designation.getDesignationTitle().trim();
        designation.setDesignationTitle(title);
        if (designation.getMinSalary() != null && designation.getMinSalary().signum() < 0) {
            throw new IllegalArgumentException("Minimum salary cannot be negative.");
        }
        if (designation.getMaxSalary() != null && designation.getMaxSalary().signum() < 0) {
            throw new IllegalArgumentException("Maximum salary cannot be negative.");
        }
        if (designation.getMinSalary() != null && designation.getMaxSalary() != null
                && designation.getMinSalary().compareTo(designation.getMaxSalary()) > 0) {
            throw new IllegalArgumentException("Minimum salary cannot exceed maximum salary.");
        }
        designationRepository.getDesignationByTitle(title, companyId)
                .filter(existing -> currentId == null
                        || !existing.getDesignationId().equals(currentId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "A designation with this title already exists in your company.");
                });
    }

    // CREATE
    public Designation createDesignation(Designation designation) {

        Long companyId = getCurrentCompanyId();

        // Never trust company ID from the request body.
        validateDesignation(designation, null, companyId);
        designation.setCompanyId(companyId);

        return designationRepository.createDesignation(
                designation, companyId);
    }

    // GET ALL
    public List<Designation> getAllDesignations() {

        Long companyId = getCurrentCompanyId();

        return designationRepository.getAllDesignations(companyId);
    }

    // GET BY ID
    public Designation getDesignationById(Integer id) {

        Long companyId = getCurrentCompanyId();

        return designationRepository.getDesignationById(id, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Designation not found with ID: " + id));
    }

    // UPDATE
    public Designation updateDesignation(
            Integer id, Designation designationDetails) {

        Long companyId = getCurrentCompanyId();

        // Ensure the designation belongs to this company and validate the new values.
        getDesignationById(id);
        validateDesignation(designationDetails, id, companyId);

        designationDetails.setCompanyId(companyId);

        return designationRepository.updateDesignation(
                id, designationDetails, companyId);
    }

    // DELETE
    public void deleteDesignation(Integer id) {

        Long companyId = getCurrentCompanyId();

        // Ensure the designation belongs to this company.
        getDesignationById(id);
        int assignedEmployees = designationRepository.countEmployeesByDesignation(id, companyId);
        if (assignedEmployees > 0) {
            throw new IllegalArgumentException(
                    "Cannot delete this designation because it is assigned to "
                            + assignedEmployees + " employee(s). Reassign those employees first.");
        }

        designationRepository.deleteDesignation(id, companyId);
    }

    // SEARCH BY TITLE
    public List<Designation> searchByTitle(String title) {

        Long companyId = getCurrentCompanyId();

        return designationRepository.searchByTitle(title, companyId);
    }

    // GET BY EXACT TITLE
    public Designation getDesignationByTitle(String title) {

        Long companyId = getCurrentCompanyId();

        return designationRepository
                .getDesignationByTitle(title, companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Designation not found with title: " + title));
    }
}