
-- 1. Create designation
CREATE OR REPLACE FUNCTION public.create_designation(
    p_title VARCHAR,
    p_min_salary NUMERIC,
    p_max_salary NUMERIC,
    p_company_id BIGINT
)
RETURNS TABLE (
    designation_id INTEGER,
    designation_title VARCHAR,
    min_salary NUMERIC,
    max_salary NUMERIC
)
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
    INSERT INTO public.designation AS d (
        designation_title, min_salary, max_salary, company_id
    )
    VALUES (
        p_title, p_min_salary, p_max_salary, p_company_id
    )
    RETURNING d.designation_id, d.designation_title,
              d.min_salary, d.max_salary;
END;
$$;


-- 2. Get all designations for a company
CREATE OR REPLACE FUNCTION public.get_all_designations(
    p_company_id BIGINT
)
RETURNS TABLE (
    designation_id INTEGER,
    designation_title VARCHAR,
    min_salary NUMERIC,
    max_salary NUMERIC
)
LANGUAGE sql
AS $$
SELECT d.designation_id, d.designation_title,
       d.min_salary, d.max_salary
FROM public.designation d
WHERE d.company_id = p_company_id
ORDER BY d.designation_id;
$$;


-- 3. Get designation by ID
CREATE OR REPLACE FUNCTION public.get_designation_by_id(
    p_id INTEGER,
    p_company_id BIGINT
)
RETURNS TABLE (
    designation_id INTEGER,
    designation_title VARCHAR,
    min_salary NUMERIC,
    max_salary NUMERIC
)
LANGUAGE sql
AS $$
SELECT d.designation_id, d.designation_title,
       d.min_salary, d.max_salary
FROM public.designation d
WHERE d.designation_id = p_id
  AND d.company_id = p_company_id;
$$;


-- 4. Get designation by title
CREATE OR REPLACE FUNCTION public.get_designation_by_title(
    p_title VARCHAR,
    p_company_id BIGINT
)
RETURNS TABLE (
    designation_id INTEGER,
    designation_title VARCHAR,
    min_salary NUMERIC,
    max_salary NUMERIC
)
LANGUAGE sql
AS $$
SELECT d.designation_id, d.designation_title,
       d.min_salary, d.max_salary
FROM public.designation d
WHERE LOWER(d.designation_title) = LOWER(p_title)
  AND d.company_id = p_company_id;
$$;


-- 5. Search designations by title
CREATE OR REPLACE FUNCTION public.search_designations_by_title(
    p_title VARCHAR,
    p_company_id BIGINT
)
RETURNS TABLE (
    designation_id INTEGER,
    designation_title VARCHAR,
    min_salary NUMERIC,
    max_salary NUMERIC
)
LANGUAGE sql
AS $$
SELECT d.designation_id, d.designation_title,
       d.min_salary, d.max_salary
FROM public.designation d
WHERE d.designation_title ILIKE '%' || p_title || '%'
      AND d.company_id = p_company_id
ORDER BY d.designation_id;
$$;


-- 6. Update designation within its company
CREATE OR REPLACE FUNCTION public.update_designation(
    p_id INTEGER,
    p_title VARCHAR,
    p_min_salary NUMERIC,
    p_max_salary NUMERIC,
    p_company_id BIGINT
)
RETURNS TABLE (
    designation_id INTEGER,
    designation_title VARCHAR,
    min_salary NUMERIC,
    max_salary NUMERIC
)
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
UPDATE public.designation AS d
SET designation_title = p_title,
    min_salary = p_min_salary,
    max_salary = p_max_salary
WHERE d.designation_id = p_id
  AND d.company_id = p_company_id
    RETURNING d.designation_id, d.designation_title,
              d.min_salary, d.max_salary;
END;
$$;


-- 7. Delete designation within its company
CREATE OR REPLACE FUNCTION public.delete_designation(
    p_id INTEGER,
    p_company_id BIGINT
)
RETURNS BOOLEAN
LANGUAGE plpgsql
AS $$
BEGIN
DELETE FROM public.designation
WHERE designation_id = p_id
  AND company_id = p_company_id;

RETURN FOUND;
END;
$$;


-- 8. Get employees with this designation in the same company
CREATE OR REPLACE FUNCTION public.get_employees_by_designation(
    p_designation_id INTEGER,
    p_company_id BIGINT
)
RETURNS SETOF public.employees
LANGUAGE sql
AS $$
SELECT e.*
FROM public.employees e
WHERE e.designation_id = p_designation_id
  AND e.company_id = p_company_id;
$$;