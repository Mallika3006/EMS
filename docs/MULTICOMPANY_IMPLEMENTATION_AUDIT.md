# Multi-company EMS implementation audit

Working branch: agent/multicompany-hardening

## Repository baseline inspected

- Package: `com.mallika.EmployeeManagementSystem`
- Primary persistence style: JDBC repositories and PostgreSQL functions. Model classes also have JPA annotations and the POM includes JPA dependencies; this is a mixed configuration that should be resolved deliberately.
- Authentication: Spring Security, `CustomUserDetailsService`, `CompanyUserDetails`, JWT cookie/Bearer filter, BCrypt.
- Tenant context: `service/CompanyContext.java` obtains company ID from authenticated `CompanyUserDetails`, rather than request parameters.
- Existing SQL scripts include some company-aware functions, but legacy unscoped functions remain.
- `ProjectRepository` calls company-aware signatures such as `get_all_projects(?)`, `get_project_by_id(?, ?)`, `update_project(?, ?, ?, ?, ?)`, and `delete_project(?, ?)`; the SQL script also contains legacy functions with different signatures. These contracts need reconciliation.
- `TaskRepository` still calls unscoped `get_all_tasks()` and `get_task_by_id(?)`. Other task operations also need a complete scope review.
- `create_task(..., p_company_id)` checks company ownership of the project and assigned employee.
- The existing `application.properties` had database credentials, a JWT signing secret, and a default Spring Security user. This branch now expects environment variables instead. Rotate any credentials or signing secrets that were committed previously.

## Required relationship model

- Never create `team`, `team_members`, `team_assignments`, or equivalent tables.
- Manager hierarchy: `employees.manager_id`.
- HR hierarchy: `employees.hr_id`.
- Project assignments: `employee_project`.
- Preserve current table names and function contracts unless a reviewed migration documents a necessary change.

## Checklist

- [x] Inspect repository tree, POM, company context, authentication service, employee repository, project/task SQL.
- [x] Create isolated working branch `agent/multicompany-hardening`.
- [x] Externalize DB credentials and JWT secret in application configuration.
- [x] Add environment-variable example.
- [ ] Inventory every table, column, SQL function, and repository invocation.
- [ ] Write versioned, backward-reviewed migrations and rollback/backup notes.
- [ ] Scope every company-owned SELECT/INSERT/UPDATE/DELETE, especially projects and tasks.
- [ ] Validate company consistency for employee, manager, HR, department, designation, project, task, attendance, leave, and payroll relationships.
- [ ] Review JWT user/company status validation, cookie flags, logout, expiration, and CSRF.
- [ ] Complete role and ownership checks for Admin, HR, Manager, and Employee.
- [ ] Add tenant-isolation and role-authorization tests.
- [ ] Run Maven compilation/tests and PostgreSQL integration tests in a configured environment.
- [ ] Build and verify the final source ZIP.

## Verification status

No Maven build or PostgreSQL integration test has been executed in this environment. GitHub APIs were used to inspect the remote repository; a local clone attempt failed because this environment could not resolve GitHub. Do not mark compilation, migrations, or integration tests as passed until they have actually run.
