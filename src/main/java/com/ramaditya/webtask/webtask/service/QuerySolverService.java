package com.ramaditya.webtask.webtask.service;

import org.springframework.stereotype.Service;

@Service
public class QuerySolverService {

    public String getFinalQuery(String regNo) {

        int lastTwo = Integer.parseInt(regNo.substring(regNo.length() - 2));

        if (lastTwo % 2 != 0) {
            return solveQuestion1();
        } else {
            return solveQuestion2();
        }
    }

    private String solveQuestion1() {
        return """
    WITH salary_data AS (
        SELECT 
            d.DEPARTMENT_NAME,
            e.EMP_ID,
            CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS EMPLOYEE_NAME,
            SUM(p.AMOUNT) AS SALARY,
            FLOOR(DATEDIFF(CURDATE(), e.DOB) / 365) AS AGE
        FROM EMPLOYEE e
        JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID
        JOIN PAYMENTS p ON e.EMP_ID = p.EMP_ID
        WHERE DAY(p.PAYMENT_TIME) != 1
        GROUP BY 
            d.DEPARTMENT_NAME,
            e.EMP_ID,
            e.FIRST_NAME,
            e.LAST_NAME,
            e.DOB
    ),
    ranked AS (
        SELECT 
            *,
            ROW_NUMBER() OVER (PARTITION BY DEPARTMENT_NAME ORDER BY SALARY DESC) AS rn
        FROM salary_data
    )
    SELECT 
        DEPARTMENT_NAME,
        SALARY,
        EMPLOYEE_NAME,
        AGE
    FROM ranked
    WHERE rn = 1;
    """;
    }


    private String solveQuestion2() {
        return """
    WITH salary_totals AS (
        SELECT 
            e.EMP_ID,
            e.FIRST_NAME,
            e.LAST_NAME,
            e.DEPARTMENT,
            SUM(p.AMOUNT) AS TOTAL_SALARY,
            FLOOR(DATEDIFF(CURDATE(), e.DOB) / 365) AS AGE
        FROM EMPLOYEE e
        JOIN PAYMENTS p ON e.EMP_ID = p.EMP_ID
        GROUP BY 
            e.EMP_ID,
            e.FIRST_NAME,
            e.LAST_NAME,
            e.DEPARTMENT,
            e.DOB
    ),
    filtered AS (
        SELECT
            s.EMP_ID,
            s.FIRST_NAME,
            s.LAST_NAME,
            s.DEPARTMENT,
            s.AGE,
            s.TOTAL_SALARY,
            d.DEPARTMENT_NAME,
            d.DEPARTMENT_ID
        FROM salary_totals s
        JOIN DEPARTMENT d ON s.DEPARTMENT = d.DEPARTMENT_ID
        WHERE s.TOTAL_SALARY > 70000
    ),
    grouped AS (
        SELECT
            DEPARTMENT_NAME,
            DEPARTMENT_ID,
            AVG(AGE) AS AVERAGE_AGE,
            GROUP_CONCAT(
                CONCAT(FIRST_NAME, ' ', LAST_NAME)
                ORDER BY FIRST_NAME SEPARATOR ', '
            ) AS EMPLOYEE_LIST
        FROM filtered
        GROUP BY DEPARTMENT_NAME, DEPARTMENT_ID
    )
    SELECT 
        DEPARTMENT_NAME,
        AVERAGE_AGE,
        SUBSTRING_INDEX(EMPLOYEE_LIST, ', ', 10) AS EMPLOYEE_LIST
    FROM grouped
    ORDER BY DEPARTMENT_ID DESC;
    """;
    }

}
