# Internal Job Posting (IJP) - Extended Microservices Application

An extended **Java Full Stack Microservices** project built for Internal Recruitment / Job Postings (IJP). The system features full recruitment lifecycle management: Application Status tracking, Interview Scheduling, In-App Notifications, Designation Master Data, and HR/Admin Candidate Management.

---

## 🌟 Architecture Overview

The system retains a decoupled **Microservices Architecture** with 5 backend applications, 1 Angular frontend, and 3 MySQL databases.

```
                         ANGULAR (ijp-frontend)
                            |
                            ↓
                       API GATEWAY
                         :8080
                            |
            ┌───────────────┼────────────────┐
            ↓               ↓                ↓
       JOB SERVICE    CANDIDATE SERVICE   ADMIN SERVICE
          :8081            :8082             :8083
            |               |                 |
            ↓               ↓                 ↓
         Job DB         Candidate DB       Admin DB
          MySQL      (Candidate,           (Admin,
                      Interview,            Designation)
                      Notification)
```

All microservices register with **Eureka Service Discovery** (`:8761`).

---

## 🚀 Features Implemented

1. **Application Status Management**:
   - Status transitions: `APPLIED` → `SHORTLISTED` → `INTERVIEW_SCHEDULED` → `SELECTED` / `REJECTED`.
2. **Admin Applicant Management**:
   - HR can view candidates, shortlist profiles, reject candidates, mark final selection, and schedule interviews.
3. **Interview Scheduling**:
   - Schedule interviews with candidate, job, date, time, venue, interviewer, and meeting link.
4. **Employee In-App Notifications**:
   - Automatic notifications dispatched to candidates when shortlisted, scheduled for interview, selected, or rejected.
5. **Angular Notifications UI**:
   - Candidate notification inbox with unread counter, detailed message view, and mark-as-read action.
6. **Designation Master Data Management**:
   - Master data management for designations inside `admin-service` (`ACTIVE`/`INACTIVE`).
   - Add Job form features a dynamic dropdown populated from active designations.

---

## 🗄️ Database Setup

Run the following SQL script in your MySQL Workbench or command line:

```sql
CREATE DATABASE IF NOT EXISTS job_db;
CREATE DATABASE IF NOT EXISTS candidate_db;
CREATE DATABASE IF NOT EXISTS admin_db;
```

---

## 🚀 How to Run the Applications

### 1. Build All Microservices
```bash
# In job-service, candidate-service, admin-service, api-gateway, service-discovery:
mvn clean package -DskipTests
```

### 2. Start Microservices in Order

1. **Service Discovery** (`service-discovery`): `mvn spring-boot:run` (`:8761`)
2. **Job Service** (`job-service`): `mvn spring-boot:run` (`:8081`)
3. **Candidate Service** (`candidate-service`): `mvn spring-boot:run` (`:8082`)
4. **Admin Service** (`admin-service`): `mvn spring-boot:run` (`:8083`)
5. **API Gateway** (`api-gateway`): `mvn spring-boot:run` (`:8080`)

### 3. Start Angular Frontend (`ijp-frontend`)
```bash
cd ijp-frontend
npm install
npm start
```
*Frontend URL*: `http://localhost:4200`
