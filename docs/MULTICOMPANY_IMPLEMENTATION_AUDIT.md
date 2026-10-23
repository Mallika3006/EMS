# Multi-company EMS implementation audit

Working branch: agent/multicompany-hardening

## Repository baseline inspected

- Package: `com.mallika.EmployeeManagementSystem`
- Primary persistence style: JDBC repositories and PostgreSQL functions. Model classes also have JPA annotations and the POM includes JPA dependencies; this is a mixed configuration that should be resolved deliberately.
- Authentication: Spring Security, `CustomUserDetailsService`, `CompanyUserDetails`, JWT cookie/Bearer filter, BCrypt.
- Tenant context: `service/CompanyContext.java` obtains company ID from authenticated `CompanyUserDetails`, rather than request parameters.
- Existing SQL scripts include company-aware and legacy unscoped functions.
- Project and task repositories now use company-scoped SQL reads/writes, avoiding the legacy unscoped project/task function signatures.
- Task create/update validates project and assigned-employee tenant ownership; manager task lists are company-scoped and exclude inactive employees.
- Leave and payroll repository operations validate employee/record ownership against the authenticated company and filter list results by employee company. Attendance repository operations use company-scoped employee joins and persist `company_id`.
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
- [x] Add company ownership checks to leave and payroll repository operations; attendance writes are company-scoped.
- [x] Restrict project writes to Admin, general task APIs to Admin/Manager, and general leave administration to Admin; preserve manager/HR-specific leave routes.
- [x] Fail closed if a username resolves to multiple users, because the current JWT subject identifies a user by username only.
- [x] Remove authentication debug output and externalize database/JWT secrets.
- [ ] Inventory every table, column, SQL function, and repository invocation completely.
- [ ] Review all legacy SQL function behavior against the new migration and verify trigger/constraint behavior on a real database.
- [ ] Review JWT user/company status validation, cookie flags, logout, expiration, and CSRF.
- [ ] Add tenant-isolation and role-authorization tests.
- [ ] Run Maven compilation/tests and PostgreSQL integration tests in a configured environment.
- [ ] Build and verify the final source ZIP.

## Migration and local verification

1. Back up the `employee_management_system` database before applying schema changes.
2. Review `sql/multicompany_hardening.sql` against the actual live schema and resolve any orphan or cross-company rows it reports.
3. Run `sql/multicompany_hardening.sql` first, then re-run the current `sql/attendance_functions.sql`, `sql/leave_functions.sql`, and `sql/payroll_functions.sql` definitions so inserts/updates populate `company_id`.
4. Configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` from `.env.example` in the environment used to start Spring Boot. Do not commit actual values.
5. Run `mvn clean test` and exercise each role with two companies before deploying.

The migration intentionally stops rather than silently assigning company IDs to orphan attendance/leave/payroll rows or accepting cross-company `employee_project` rows. It creates no team-related tables.

## Verification status

No Maven build or PostgreSQL integration test has been executed in this environment. GitHub APIs were used to inspect and update the remote branch; a local clone attempt failed because this environment could not resolve GitHub. The changes are committed, but compilation, migration execution, and integration behavior remain unverified until run against the real project/database.
