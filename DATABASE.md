# Database Specification & Design

## Overview
Following microservice best practices, each microservice maintains a dedicated MySQL database schema.

---

## 1. Database: `job_db`

### Table: `job_posting`

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Surrogate Primary Key |
| `job_id` | VARCHAR(255) | NOT NULL | Unique Business Identifier (e.g. JOB101) |
| `description` | TEXT | NULLABLE | Detailed job responsibilities |
| `designation` | VARCHAR(255) | NOT NULL | Title of the position |
| `location` | VARCHAR(255) | NOT NULL | Office location / work mode |
| `skill_set` | VARCHAR(255) | NOT NULL | Required skills (comma-separated) |
| `experience` | VARCHAR(255) | NOT NULL | Experience criteria (e.g. 2-4 years) |
| `salary_min` | DOUBLE | NOT NULL | Minimum salary range |
| `salary_max` | DOUBLE | NOT NULL | Maximum salary range |
| `status` | VARCHAR(50) | NOT NULL | Job status (`OPEN`, `CLOSED`) |

---

## 2. Database: `candidate_db`

### Table: `candidate`

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Surrogate Primary Key |
| `first_name` | VARCHAR(255) | NOT NULL | Candidate First Name |
| `last_name` | VARCHAR(255) | NOT NULL | Candidate Last Name |
| `employee_id` | VARCHAR(255) | UNIQUE, NOT NULL | Internal Employee ID |
| `dob` | VARCHAR(255) | NOT NULL | Date of Birth |
| `email` | VARCHAR(255) | UNIQUE, NOT NULL | Official Email Address |
| `job_id` | BIGINT | NOT NULL | Foreign key reference (stored as plain ID) |

> **Note**: `job_id` is a simple numerical column. No JPA entity relation (`@ManyToOne`) is declared across microservices.

---

## 3. Database: `admin_db`

### Table: `admin`

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Surrogate Primary Key |
| `email` | VARCHAR(255) | UNIQUE, NOT NULL | Admin Email Address |
| `password` | VARCHAR(255) | NOT NULL | Admin Password |
