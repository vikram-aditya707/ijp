# Architecture Guide: Internal Job Posting (IJP)

## 1. Why Microservices Architecture?
In a monolithic application, all features (jobs, candidates, admin) reside in a single codebase and share one database. If one component experiences high load or fails, the whole application goes down.

In our **Microservices Architecture**:
- Each business domain (Jobs, Candidates, Admin) is an independent Spring Boot application.
- Services can be scaled, deployed, and updated independently.
- Failure in one service does not crash the rest of the application.

---

## 2. Service Discovery (Eureka Server)
- **Role**: Service registry running on port `8761`.
- **Why Eureka?**: In a distributed system, services may run on dynamic hostnames/ports. Instead of hardcoding IP addresses, microservices register themselves with Eureka upon startup. The Gateway and other microservices query Eureka to locate instances automatically.

---

## 3. API Gateway (Spring Cloud Gateway)
- **Role**: Single entry point for clients running on port `8080`.
- **Why API Gateway?**: 
  - Prevents Angular from making direct requests to ports `8081`, `8082`, and `8083`.
  - Simplifies security and CORS handling at a single entry point.
  - Dynamically routes requests using logical service names:
    - `/api/jobs/**` → `lb://JOB-SERVICE`
    - `/api/candidates/**` → `lb://CANDIDATE-SERVICE`
    - `/api/admin/**` → `lb://ADMIN-SERVICE`

---

## 4. Database-per-Service Pattern
Each microservice owns its data store:
- `job-service` → `job_db`
- `candidate-service` → `candidate_db`
- `admin-service` → `admin_db`

**Why?**
Direct database joins across microservices create tight coupling. By enforcing separate databases, services remain completely autonomous.

---

## 5. Microservice-to-Microservice Communication
When a candidate applies for a job:
1. `Candidate` submits application form to Angular.
2. Angular sends `POST /api/candidates` to API Gateway (`:8080`).
3. Gateway forwards request to `Candidate Service` (`:8082`).
4. `Candidate Service` validates duplicate email and employee ID locally.
5. `Candidate Service` calls `Job Service` (`:8081`) via OpenFeign REST client (`GET /api/jobs/{id}`).
6. `Job Service` responds with job details.
7. `Candidate Service` verifies the job status is `"OPEN"`.
8. Candidate record is saved in `candidate_db`.
