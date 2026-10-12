-- 1. CREATE USER
CREATE OR REPLACE FUNCTION create_user(
    p_username VARCHAR,
    p_password VARCHAR,
    p_is_active BOOLEAN,
    p_employee_id INTEGER,
    p_role_id INTEGER
)
RETURNS TABLE (
    user_id INTEGER,
    username VARCHAR,
    password VARCHAR,
    is_active BOOLEAN,
    employee_id INTEGER,
    role_id INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
    INSERT INTO users (
        username,
        password,
        is_active,
        employee_id,
        role_id
    )
    VALUES (
        p_username,
        p_password,
        p_is_active,
        p_employee_id,
        p_role_id
    )
    RETURNING
        users.user_id,
        users.username,
        users.password,
        users.is_active,
        users.employee_id,
        users.role_id;
END;
$$;


-- 2. GET ALL USERS
CREATE OR REPLACE FUNCTION get_all_users()
RETURNS TABLE (
    user_id INTEGER,
    username VARCHAR,
    password VARCHAR,
    is_active BOOLEAN,
    employee_id INTEGER,
    role_id INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT
    u.user_id,
    u.username,
    u.password,
    u.is_active,
    u.employee_id,
    u.role_id
FROM users u
ORDER BY u.user_id;
END;
$$;


-- 3. GET USER BY ID
CREATE OR REPLACE FUNCTION get_user_by_id(
    p_id INTEGER
)
RETURNS TABLE (
    user_id INTEGER,
    username VARCHAR,
    password VARCHAR,
    is_active BOOLEAN,
    employee_id INTEGER,
    role_id INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT
    u.user_id,
    u.username,
    u.password,
    u.is_active,
    u.employee_id,
    u.role_id
FROM users u
WHERE u.user_id = p_id;
END;
$$;


-- 4. UPDATE USER
CREATE OR REPLACE FUNCTION update_user(
    p_id INTEGER,
    p_username VARCHAR,
    p_password VARCHAR,
    p_is_active BOOLEAN,
    p_employee_id INTEGER,
    p_role_id INTEGER
)
RETURNS TABLE (
    user_id INTEGER,
    username VARCHAR,
    password VARCHAR,
    is_active BOOLEAN,
    employee_id INTEGER,
    role_id INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
UPDATE users u
SET
    username = p_username,
    password = p_password,
    is_active = p_is_active,
    employee_id = p_employee_id,
    role_id = p_role_id
WHERE u.user_id = p_id
    RETURNING
        u.user_id,
        u.username,
        u.password,
        u.is_active,
        u.employee_id,
        u.role_id;
END;
$$;


-- 5. DELETE USER
CREATE OR REPLACE FUNCTION delete_user(
    p_id INTEGER
)
RETURNS BOOLEAN
LANGUAGE plpgsql
AS $$
BEGIN
DELETE FROM users
WHERE user_id = p_id;

RETURN FOUND;
END;
$$;


-- 6. EXACT USERNAME
CREATE OR REPLACE FUNCTION get_user_by_username(
    p_username VARCHAR
)
RETURNS TABLE (
    user_id INTEGER,
    username VARCHAR,
    password VARCHAR,
    is_active BOOLEAN,
    employee_id INTEGER,
    role_id INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT
    u.user_id,
    u.username,
    u.password,
    u.is_active,
    u.employee_id,
    u.role_id
FROM users u
WHERE LOWER(u.username) = LOWER(p_username);
END;
$$;


-- 7. FIND BY USERNAME
-- Kept separately because your existing repository
-- has both findByUsernameIgnoreCase and findByUsername.
CREATE OR REPLACE FUNCTION find_user_by_username(
    p_username VARCHAR
)
RETURNS TABLE (
    user_id INTEGER,
    username VARCHAR,
    password VARCHAR,
    is_active BOOLEAN,
    employee_id INTEGER,
    role_id INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT
    u.user_id,
    u.username,
    u.password,
    u.is_active,
    u.employee_id,
    u.role_id
FROM users u
WHERE u.username = p_username;
END;
$$;


-- 8. SEARCH USERNAME
CREATE OR REPLACE FUNCTION search_users_by_username(
    p_username VARCHAR
)
RETURNS TABLE (
    user_id INTEGER,
    username VARCHAR,
    password VARCHAR,
    is_active BOOLEAN,
    employee_id INTEGER,
    role_id INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT
    u.user_id,
    u.username,
    u.password,
    u.is_active,
    u.employee_id,
    u.role_id
FROM users u
WHERE u.username ILIKE '%' || p_username || '%'
ORDER BY u.user_id;
END;
$$;


-- 9. ACTIVE / INACTIVE
CREATE OR REPLACE FUNCTION get_users_by_status(
    p_is_active BOOLEAN
)
RETURNS TABLE (
    user_id INTEGER,
    username VARCHAR,
    password VARCHAR,
    is_active BOOLEAN,
    employee_id INTEGER,
    role_id INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT
    u.user_id,
    u.username,
    u.password,
    u.is_active,
    u.employee_id,
    u.role_id
FROM users u
WHERE u.is_active = p_is_active
ORDER BY u.user_id;
END;
$$;


-- 10. USER BY EMPLOYEE
CREATE OR REPLACE FUNCTION get_user_by_employee(
    p_employee_id INTEGER
)
RETURNS TABLE (
    user_id INTEGER,
    username VARCHAR,
    password VARCHAR,
    is_active BOOLEAN,
    employee_id INTEGER,
    role_id INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT
    u.user_id,
    u.username,
    u.password,
    u.is_active,
    u.employee_id,
    u.role_id
FROM users u
WHERE u.employee_id = p_employee_id;
END;
$$;


-- 11. USERS BY ROLE
CREATE OR REPLACE FUNCTION get_users_by_role(
    p_role_id INTEGER
)
RETURNS TABLE (
    user_id INTEGER,
    username VARCHAR,
    password VARCHAR,
    is_active BOOLEAN,
    employee_id INTEGER,
    role_id INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT
    u.user_id,
    u.username,
    u.password,
    u.is_active,
    u.employee_id,
    u.role_id
FROM users u
WHERE u.role_id = p_role_id
ORDER BY u.user_id;
END;
$$;


-- 12. USERS BY ROLE + STATUS
CREATE OR REPLACE FUNCTION get_users_by_role_and_status(
    p_role_id INTEGER,
    p_is_active BOOLEAN
)
RETURNS TABLE (
    user_id INTEGER,
    username VARCHAR,
    password VARCHAR,
    is_active BOOLEAN,
    employee_id INTEGER,
    role_id INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT
    u.user_id,
    u.username,
    u.password,
    u.is_active,
    u.employee_id,
    u.role_id
FROM users u
WHERE u.role_id = p_role_id
  AND u.is_active = p_is_active
ORDER BY u.user_id;
END;
$$;


-- 13. SORT USERS
CREATE OR REPLACE FUNCTION sort_users(
    p_field VARCHAR,
    p_direction VARCHAR
)
RETURNS TABLE (
    user_id INTEGER,
    username VARCHAR,
    password VARCHAR,
    is_active BOOLEAN,
    employee_id INTEGER,
    role_id INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN

    IF LOWER(p_field) = 'userid'
       AND LOWER(p_direction) = 'desc' THEN

        RETURN QUERY
SELECT u.user_id, u.username, u.password,
       u.is_active, u.employee_id, u.role_id
FROM users u
ORDER BY u.user_id DESC;

ELSIF LOWER(p_field) = 'userid' THEN

        RETURN QUERY
SELECT u.user_id, u.username, u.password,
       u.is_active, u.employee_id, u.role_id
FROM users u
ORDER BY u.user_id ASC;

ELSIF LOWER(p_field) = 'username'
          AND LOWER(p_direction) = 'desc' THEN

        RETURN QUERY
SELECT u.user_id, u.username, u.password,
       u.is_active, u.employee_id, u.role_id
FROM users u
ORDER BY u.username DESC;

ELSIF LOWER(p_field) = 'username' THEN

        RETURN QUERY
SELECT u.user_id, u.username, u.password,
       u.is_active, u.employee_id, u.role_id
FROM users u
ORDER BY u.username ASC;

ELSIF LOWER(p_field) = 'isactive'
          AND LOWER(p_direction) = 'desc' THEN

        RETURN QUERY
SELECT u.user_id, u.username, u.password,
       u.is_active, u.employee_id, u.role_id
FROM users u
ORDER BY u.is_active DESC;

ELSE

        RETURN QUERY
SELECT u.user_id, u.username, u.password,
       u.is_active, u.employee_id, u.role_id
FROM users u
ORDER BY u.is_active ASC;

END IF;

END;
$$;


-- Multi-company login lookup
CREATE OR REPLACE FUNCTION public.find_user_by_username_company(
    p_username VARCHAR
)
RETURNS TABLE (
    user_id INTEGER,
    username VARCHAR,
    password VARCHAR,
    is_active BOOLEAN,
    employee_id INTEGER,
    role_id INTEGER,
    company_id BIGINT
)
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT
    u.user_id,
    u.username,
    u.password,
    u.is_active,
    u.employee_id,
    u.role_id,
    u.company_id
FROM public.users u
WHERE u.username = p_username
    LIMIT 1;
END;
$$;
