# Multi-company EMS implementation audit

Working branch: agent/multicompany-hardening

## Repository baseline inspected

- Package: `com.mallika.EmployeeManagementSystem`
- Primary persistence style: JDBC repositories and PostgreSQL functions. Model classes also have JPA annotations and the POM includes JPA dependencies; this is a mixed configuration that should be resolved deliberately.
- Authentication: Spring Security, `CustomUserDetailsService`, `CompanyUserDetails`, JWT cookie/Bearer filter, BCrypt.
- Tenant context: `service/CompanyContext.java` obtains company ID from authenticated `CompanyUserDetails`, rather than request parameters.
- Existing SQL scripts include company-aware and legacy unscoped functions. A new `sql/department_company_functions.sql` now supplies the company-scoped department functions called by `DepartmentRepository`; the existing `sql/department_functions.sql` contains the company's designation functions despite its filename.
- Project and task repositories now use company-scoped SQL reads/writes, avoiding the legacy unscoped project/task function signatures.
- Task create/update validates project and assigned-employee tenant ownership; manager task lists are company-scoped and exclude inactive employees.
- Managers can edit/delete only projects assigned to them, and assign their own projects to active direct reports through `POST /projects/{projectId}/members/{employeeId}`. The manager projects page now includes a member-assignment control backed by the existing `employee_project` table.
- Leave and payroll repositories call company-ID-bearing SQL overloads for reads and writes. Attendance repository operations use company-scoped employee joins and persist `company_id`.
- The original `application.properties` contained database credentials, a JWT signing secret, and a default Spring Security user. This branch now expects environment variables instead. Rotate any credentials or signing secrets that were committed previously.

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
- [x] Add a company registry and SQL migration to backfill tenant IDs for existing data, add tenant indexes, and add company/relationship constraints.
- [x] Scope task/project CRUD and list/search/filter/sort paths by authenticated company context.
- [x] Add company-scoped manager project-member assignment using `employee_project` and expose it in the manager projects page.
- [x] Allow managers to edit/delete only projects assigned to them; Admin alone creates projects.
- [x] Add the missing company-scoped department functions used by `DepartmentRepository`.
- [x] Add company ownership checks and company-scoped SQL overloads for leave/payroll operations; attendance writes are company-scoped.
- [x] Restrict project writes to Admin, general task APIs to Admin/Manager, and general leave administration to Admin; preserve manager/HR-specific leave routes.
- [x] Fail closed if a username resolves to multiple users, because the current JWT subject identifies a user by username only.
- [x] Remove authentication debug output and externalize database/JWT secrets.
- [ ] Inventory every table, column, SQL function, and repository invocation completely.
- [ ] Review all legacy SQL function behavior against the new migration and verify trigger/constraint behavior on a real database.
- [ ] Review JWT user/company status validation, cookie flags, logout, expiration, and CSRF.
- [x] Add database-level tenant-isolation assertions for migration, project membership, and leave/payroll queries.
- [ ] Add application-level role-authorization tests.
- [x] Compile and package with Java 26 in GitHub Actions.
- [x] Run the company migration, all updated SQL function scripts, and tenant-isolation assertions against a disposable PostgreSQL 18 fixture in GitHub Actions.
- [ ] Run application-level tests and exercise all roles against the real EMS schema/data.
- [ ] Build and verify the final source ZIP.

## Migration and local verification

1. Back up the `employee_management_system` database before applying schema changes.
2. Review `sql/multicompany_hardening.sql` against the actual live schema. Resolve duplicate usernames, orphan records, or cross-company relationships it reports before retrying.
3. Run `sql/multicompany_hardening.sql` first. Then run `sql/department_company_functions.sql`, `sql/department_functions.sql` (company-aware designation overloads), `sql/task_functions.sql` (company-aware create function), the updated `sql/attendance_functions.sql`, `sql/leave_functions.sql`, and `sql/payroll_functions.sql`, and finally `sql/tenant_scoped_leave_payroll_functions.sql` so leave/payroll repositories use company-ID-bearing overloads.
4. Configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` from `.env.example` in the environment used to start Spring Boot. Do not commit actual values.
5. Run `mvn clean test` and exercise each role with two companies before deploying.

The migration intentionally stops rather than silently assigning company IDs to orphan attendance/leave/payroll rows, accepting cross-company relationships, or allowing duplicate usernames (the JWT subject is username-only). It creates no team-related tables.

New-company onboarding is currently a database/bootstrap operation: create a row in `companies`, then provision the first Admin user with that company ID through a trusted administrative process. Do not accept a company ID from a public login or employee-create request.

## Verification status

- **Maven verify:** passed in GitHub Actions on Java 26. Production and test sources compile and the Spring Boot artifact packages successfully; the repository currently has no executable application-level test methods.
- **PostgreSQL migration/functions:** passed against a disposable PostgreSQL 18 fixture containing two companies and a manager/HR hierarchy. The workflow validates company-ID backfill, foreign-key creation, rejection of cross-company project membership, company-scoped leave/payroll query overloads, manager/HR list filtering, and successful creation of the department/designation/task/attendance/leave/payroll SQL functions.
- **Not yet verified:** running the migration against the real EMS database, existing-data edge cases, application startup with the real schema, browser flows, and end-to-end role/tenant integration tests.

The migration is intentionally tested against a representative fixture, not claimed safe for the live database until it is backed up and checked against its actual data.
