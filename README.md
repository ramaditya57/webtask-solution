# Webtask – Spring Boot Webhook SQL Automation

A Spring Boot application built for the HealthRX hiring challenge that automates webhook generation and SQL query submission based on registration number logic.

---

## Overview

This application automatically:

1. Sends a POST request on startup to generate a webhook
2. Determines the SQL question based on the last two digits of the registration number
3. Generates the correct SQL query for Question 1 or Question 2
4. Submits the generated SQL query to the webhook URL using JWT authorization

The entire process runs automatically on application startup using `CommandLineRunner`, with no controller endpoints required.

---

## Technologies Used

- **Java 17**
- **Spring Boot 3.x**
- **RestTemplate** (HTTP communication)
- **Maven** (Build tool)
- **Jackson** (JSON processing)

---

## Project Structure

```
webtask/
├── src/main/java/com/.../webtask/
│   ├── WebtaskApplication.java           # Main class + RestTemplate bean
│   ├── service/
│   │   ├── StartupService.java           # Startup automation + webhook logic
│   │   └── QuerySolverService.java       # SQL query generation logic
│   └── model/
│       ├── GenerateWebhookRequest.java   # Request DTO
│       ├── GenerateWebhookResponse.java  # Response DTO
│       └── FinalQueryRequest.java        # Final submission DTO
├── src/main/resources/
│   └── application.properties            # Configuration
├── pom.xml                               # Maven dependencies
└── target/
    └── webtask-0.0.1-SNAPSHOT.jar        # Built executable JAR
```

---

## How It Works

### Step 1: User Input on Startup

The application prompts for:

```
Enter your name:
Enter your regNo:
Enter your email:
```

These details are sent to:

```
POST https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA
```

**Request Body:**

```json
{
  "name": "Ramaditya",
  "regNo": "REG12347",
  "email": "ramaditya@gmail.com"
}
```

### Step 2: Webhook Generation Response

The API returns:

- `webhook` → URL to submit the SQL query
- `accessToken` → JWT token for authorization

### Step 3: Question Selection Logic

The application determines which SQL question to solve based on the **last two digits** of the registration number:

| Last Two Digits | Question Assigned |
|----------------|-------------------|
| Odd            | Question 1        |
| Even           | Question 2        |

**Example:**  
`REG12347` → `47` (Odd) → **Question 1**

### Step 4: SQL Query Submission

The application sends the generated SQL query:

```
POST <webhook_url>
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Request Body:**

```json
{
  "finalQuery": "<generated_sql_query>"
}
```

---

## SQL Solutions

### Question 1: Highest Paid Employee per Department

**Requirements:**
- Exclude payments made on the 1st of any month
- Calculate employee age from date of birth
- Return the highest-paid employee per department

```sql
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
```

### Question 2: Employees with Salary > 70,000

**Requirements:**
- Filter employees earning more than 70,000
- Calculate average age per department
- List up to 10 employees per department
- Order departments by ID in descending order

```sql
WITH salary_filtered AS (
    SELECT 
        d.DEPARTMENT_ID,
        d.DEPARTMENT_NAME,
        CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS EMPLOYEE_NAME,
        FLOOR(DATEDIFF(CURDATE(), e.DOB) / 365) AS AGE,
        p.AMOUNT
    FROM EMPLOYEE e
    JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID
    JOIN PAYMENTS p ON e.EMP_ID = p.EMP_ID
    WHERE p.AMOUNT > 70000
),
grouped AS (
    SELECT 
        DEPARTMENT_ID,
        DEPARTMENT_NAME,
        AVG(AGE) AS AVERAGE_AGE,
        GROUP_CONCAT(EMPLOYEE_NAME ORDER BY EMPLOYEE_NAME SEPARATOR ', ') AS EMPLOYEE_LIST
    FROM salary_filtered
    GROUP BY DEPARTMENT_ID, DEPARTMENT_NAME
)
SELECT 
    DEPARTMENT_NAME,
    AVERAGE_AGE,
    SUBSTRING_INDEX(EMPLOYEE_LIST, ', ', 10) AS EMPLOYEE_LIST
FROM grouped
ORDER BY DEPARTMENT_ID DESC;
```

---

## Build and Run Instructions

### Prerequisites

- JDK 17 or higher
- Maven 3.6+

### Build the Application

From the project root directory:

```bash
mvn clean package
```

This creates the executable JAR at:

```
target/webtask-0.0.1-SNAPSHOT.jar
```

### Run the Application

```bash
java -jar target/webtask-0.0.1-SNAPSHOT.jar
```

The application will:
1. Prompt for your name, registration number, and email
2. Automatically generate the webhook
3. Determine and execute the appropriate SQL query
4. Submit the result to the webhook URL

---

## Submission Checklist

### Required Items

- ✅ Public GitHub repository with complete source code
- ✅ Final JAR file in `/releases` or `/target` directory
- ✅ This README.md file

### GitHub Repository Format

```
https://github.com/<your-username>/webtask-solution
```


## Key Features

✅ Automated startup execution using `CommandLineRunner`  
✅ Dynamic question selection based on registration number  
✅ JWT-based authentication for webhook submission  
✅ No manual controller endpoints required  
✅ Clean separation of concerns with service layer architecture  
✅ Production-ready JAR build with Maven  
