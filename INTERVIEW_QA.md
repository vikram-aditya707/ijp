# Interview Questions & Answers Guide

This document prepares you to present and defend the **Internal Job Posting (IJP)** Microservices Project during technical interviews and project presentations.

---

### Q1: What is microservices architecture?
**Answer**: Microservices architecture is an architectural style that structures an application as a collection of small, autonomous services modeled around specific business domains. Each service runs in its own process, communicates via lightweight mechanisms (REST APIs), and can be developed, deployed, and scaled independently.

### Q2: Why did you use microservices for this project?
**Answer**: To decouple domain boundaries:
1. `job-service`, `candidate-service`, and `admin-service` operate independently.
2. If `candidate-service` faces heavy traffic during a hiring drive, it can be scaled independently without overloading `job-service`.
3. If one database or service fails, the entire application doesn't crash.

### Q3: What is Eureka? Why do we need service discovery?
**Answer**: Eureka is a Netflix-developed Service Discovery Server. In cloud and microservice environments, service IP addresses and ports can change dynamically. Eureka acts as a registry where microservices publish their network locations so that API Gateways and other microservices can discover them automatically without hardcoded URLs.

### Q4: What is API Gateway? Why don't Angular clients directly call every service?
**Answer**: Spring Cloud Gateway acts as a single entry point for all frontend client requests (running on port `8080`). Direct client-to-service calls would require Angular to manage multiple backend URLs (8081, 8082, 8083), handle cross-origin resource sharing (CORS) across multiple servers, and expose internal infrastructure. The Gateway routes traffic based on URL paths (`/api/jobs/**`, `/api/candidates/**`).

### Q5: What is inter-service communication? How does Candidate Service communicate with Job Service?
**Answer**: Inter-service communication happens when one microservice needs data from another microservice. In our project, `Candidate Service` calls `Job Service` via OpenFeign REST client (`@FeignClient(name = "job-service")`) to verify that the requested `jobId` exists and that its status is `"OPEN"` before creating a candidate record.

### Q6: Why does each service have its own database?
**Answer**: Enforcing the Database-per-Service pattern guarantees loose coupling. If microservices share a single SQL database, changing a table schema in one domain could inadvertently break another domain.

### Q7: What is Spring Boot, Spring Data JPA, and Hibernate?
**Answer**:
- **Spring Boot**: Framework for building production-ready Java applications with embedded Tomcat and auto-configuration.
- **Spring Data JPA**: Abstraction layer that reduces boilerplate repository code by providing pre-built CRUD methods (`JpaRepository`).
- **Hibernate**: Object-Relational Mapping (ORM) framework that implements JPA and maps Java objects to MySQL database tables.

### Q8: What are `@RestController`, `@Service`, `@Repository`, and `@Entity`?
**Answer**:
- `@RestController`: Marks a class as a REST controller handling HTTP requests and returning JSON responses.
- `@Service`: Marks a class containing business logic.
- `@Repository`: Marks a Data Access Object (DAO) interface handling database interactions.
- `@Entity`: Marks a Java class as a persistent database table model.

### Q9: What happens if Job Service is unavailable when a candidate applies?
**Answer**: The `Candidate Service` tries to contact `Job Service` via Feign client. Since `Job Service` is down, a REST client exception is caught, and `Candidate Service` responds with an error message informing the user that job validation failed, protecting database consistency.

### Q10: What happens when a candidate applies for a closed job?
**Answer**: `Candidate Service` fetches the job details from `Job Service`. If `status.equalsIgnoreCase("CLOSED")`, `Candidate Service` throws a runtime exception (`Cannot apply: Job is CLOSED!`) and returns an HTTP 400 Bad Request to Angular with a descriptive error alert.
