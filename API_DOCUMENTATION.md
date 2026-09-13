# Extended API Documentation: Internal Job Posting (IJP)

All API calls route through **API Gateway** running at `http://localhost:8080`.

---

## 1. Candidate Service New Endpoints (`/api/candidates/**`)

### 1. Update Candidate Status
- **HTTP Method**: `PUT`
- **URL**: `http://localhost:8080/api/candidates/{id}/status`
- **Request Body**:
```json
{
    "status": "SHORTLISTED"
}
```
*(Valid status values: `APPLIED`, `SHORTLISTED`, `INTERVIEW_SCHEDULED`, `SELECTED`, `REJECTED`)*

### 2. Schedule Interview
- **HTTP Method**: `POST`
- **URL**: `http://localhost:8080/api/candidates/interviews`
- **Request Body**:
```json
{
    "candidateId": 1,
    "jobId": 1,
    "interviewDate": "2026-08-20",
    "interviewTime": "11:00 AM",
    "location": "Meeting Room 2",
    "interviewer": "Senior Tech Lead",
    "meetingLink": "https://meet.google.com/xyz-abc-def"
}
```

### 3. Get Candidate Notifications
- **HTTP Method**: `GET`
- **URL**: `http://localhost:8080/api/candidates/notifications/{candidateId}`

### 4. Get Unread Notification Count
- **HTTP Method**: `GET`
- **URL**: `http://localhost:8080/api/candidates/notifications/{candidateId}/unread-count`

### 5. Mark Notification as Read
- **HTTP Method**: `PUT`
- **URL**: `http://localhost:8080/api/candidates/notifications/{id}/read`

---

## 2. Admin Service Designation Master Endpoints (`/api/admin/designations/**`)

### 1. Add Designation
- **HTTP Method**: `POST`
- **URL**: `http://localhost:8080/api/admin/designations`
- **Request Body**:
```json
{
    "name": "Cloud Solutions Architect",
    "status": "ACTIVE"
}
```

### 2. Get All Designations
- **HTTP Method**: `GET`
- **URL**: `http://localhost:8080/api/admin/designations`

### 3. Get Active Designations
- **HTTP Method**: `GET`
- **URL**: `http://localhost:8080/api/admin/designations/active`

### 4. Update Designation Status (Activate/Deactivate)
- **HTTP Method**: `PUT`
- **URL**: `http://localhost:8080/api/admin/designations/{id}/status`
- **Request Body**:
```json
{
    "status": "INACTIVE"
}
```
