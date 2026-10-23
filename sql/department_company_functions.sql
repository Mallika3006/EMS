-- Company-scoped department functions used by DepartmentRepository.
-- Apply after sql/multicompany_hardening.sql.

CREATE OR REPLACE FUNCTION public.create_department(
    p_department_name VARCHAR,
    p_location VARCHAR,
    p_description VARCHAR,
    p_company_id BIGINT
)
RETURNS SETOF public.department
LANGUAGE plpgsql
AS $$
BEGIN
    IF p_company_id IS NULL OR NOT EXISTS (
        SELECT 1 FROM public.companies WHERE company_id = p_company_id
    ) THEN
        RAISE EXCEPTION 'A valid company ID is required';
    END IF;

    RETURN QUERY
    INSERT INTO public.department (
        department_name, location, description, company_id
    )
    VALUES (
        p_department_name, p_location, p_description, p_company_id
    )
    RETURNING *;
END;
$$;

CREATE OR REPLACE FUNCTION public.get_all_departments(p_company_id BIGINT)
RETURNS SETOF public.department
LANGUAGE sql
STABLE
AS $$
    SELECT d.*
    FROM public.department d
    WHERE d.company_id = p_company_id
    ORDER BY d.department_id;
$$;

CREATE OR REPLACE FUNCTION public.get_department_by_id(
    p_id INTEGER, p_company_id BIGINT
)
RETURNS SETOF public.department
LANGUAGE sql
STABLE
AS $$
    SELECT d.*
    FROM public.department d
    WHERE d.department_id = p_id
      AND d.company_id = p_company_id;
$$;

CREATE OR REPLACE FUNCTION public.update_department(
    p_id INTEGER,
    p_department_name VARCHAR,
    p_location VARCHAR,
    p_description VARCHAR,
    p_company_id BIGINT
)
RETURNS SETOF public.department
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    UPDATE public.department d
    SET department_name = p_department_name,
        location = p_location,
        description = p_description
    WHERE d.department_id = p_id
      AND d.company_id = p_company_id
    RETURNING d.*;
END;
$$;

CREATE OR REPLACE FUNCTION public.delete_department(
    p_id INTEGER, p_company_id BIGINT
)
RETURNS BOOLEAN
LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM public.department
    WHERE department_id = p_id
      AND company_id = p_company_id;
    RETURN FOUND;
END;
$$;

CREATE OR REPLACE FUNCTION public.search_departments_by_name(
    p_name VARCHAR, p_company_id BIGINT
)
RETURNS SETOF public.department
LANGUAGE sql
STABLE
AS $$
    SELECT d.*
    FROM public.department d
    WHERE d.company_id = p_company_id
      AND d.department_name ILIKE '%' || p_name || '%'
    ORDER BY d.department_id;
$$;

CREATE OR REPLACE FUNCTION public.get_department_by_name(
    p_name VARCHAR, p_company_id BIGINT
)
RETURNS SETOF public.department
LANGUAGE sql
STABLE
AS $$
    SELECT d.*
    FROM public.department d
    WHERE d.company_id = p_company_id
      AND LOWER(d.department_name) = LOWER(p_name)
    ORDER BY d.department_id
    LIMIT 1;
$$;

CREATE OR REPLACE FUNCTION public.search_departments_by_location(
    p_location VARCHAR, p_company_id BIGINT
)
RETURNS SETOF public.department
LANGUAGE sql
STABLE
AS $$
    SELECT d.*
    FROM public.department d
    WHERE d.company_id = p_company_id
      AND d.location ILIKE '%' || p_location || '%'
    ORDER BY d.department_id;
$$;
