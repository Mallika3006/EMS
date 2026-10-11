
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

    // CREATE
    public Designation createDesignation(Designation designation) {

        Long companyId = getCurrentCompanyId();

        // Never trust company ID from the request body
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

        // Ensure the designation belongs to this company
        getDesignationById(id);

        designationDetails.setCompanyId(companyId);

        return designationRepository.updateDesignation(
                id, designationDetails, companyId);
    }

    // DELETE
    public void deleteDesignation(Integer id) {

        Long companyId = getCurrentCompanyId();

        // Ensure the designation belongs to this company
        getDesignationById(id);

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