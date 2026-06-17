# Assessment Service — API Endpoints Testing Guide

Base URL: `http://localhost:8083`

All requests (except Swagger) require these headers:
```
X-User-Id: <userId>
X-User-Role: INSTRUCTOR | LEARNER | ADMIN
Authorization: Bearer <jwt-token>
```

---

## 1. Assignments — `/api/v1/assignments`

### 1.1 Create Assignment
```
POST /api/v1/assignments
Content-Type: multipart/form-data
X-User-Id: 1
X-User-Role: INSTRUCTOR

Part "request" (application/json):
{
  "title": "Java Collections Framework",
  "description": "Create a REST API with CRUD operations",
  "courseId": "101",
  "totalMarks": 100.0,
  "passMarks": 40.0,
  "assignmentType": "FILE_UPLOAD",
  "difficultyLevel": "INTERMEDIATE",
  "dueDate": "2026-07-30"
}
Part "file": (optional PDF/DOC file)
```
Response: `201 Created` → AssignmentResponse

---

### 1.2 Get Assignment by ID
```
GET /api/v1/assignments/{id}
X-User-Id: 1
X-User-Role: INSTRUCTOR

Example: GET /api/v1/assignments/abc-123
```
Response: `200 OK` → AssignmentResponse

---

### 1.3 Get All Assignments
```
GET /api/v1/assignments
X-User-Id: 1
X-User-Role: INSTRUCTOR
```
Response: `200 OK` → List<AssignmentResponse>

---

### 1.4 Get Assignments by Course
```
GET /api/v1/assignments/courses/{courseId}
X-User-Id: 1
X-User-Role: INSTRUCTOR

Example: GET /api/v1/assignments/courses/101
```
Response: `200 OK` → List<AssignmentResponse>

---

### 1.5 Get Assignments by Instructor
```
GET /api/v1/assignments/instructors/{instructorId}
X-User-Id: 1
X-User-Role: INSTRUCTOR

Example: GET /api/v1/assignments/instructors/1
```
Response: `200 OK` → List<AssignmentResponse>

---

### 1.6 Update Assignment
```
PUT /api/v1/assignments/{id}
Content-Type: multipart/form-data
X-User-Id: 1
X-User-Role: INSTRUCTOR

Part "request" (application/json):
{
  "title": "Updated Title",
  "totalMarks": 150.0,
  "passMarks": 60.0,
  "status": "PUBLISHED",
  "difficultyLevel": "ADVANCED",
  "dueDate": "2026-08-15"
}
Part "file": (optional — replaces existing file)
```
Response: `200 OK` → AssignmentResponse

---

### 1.7 Delete Assignment
```
DELETE /api/v1/assignments/{id}
X-User-Id: 1
X-User-Role: INSTRUCTOR

Example: DELETE /api/v1/assignments/abc-123
```
Response: `204 No Content`

---

## 2. Submissions — `/api/v1/submissions`

### 2.1 Submit Assignment
```
POST /api/v1/submissions
Content-Type: multipart/form-data
X-User-Id: 42
X-User-Role: LEARNER

Part "request" (application/json):
{
  "assignmentId": "abc-123",
  "content": "My text submission (optional)"
}
Part "file": (student's PDF/DOC file)
```
Response: `201 Created` → SubmissionResponse

Note: studentId is taken from X-User-Id header automatically.
Assignment must be PUBLISHED. Student must be enrolled in the course.

---

### 2.2 Get Submission by ID
```
GET /api/v1/submissions/{id}
X-User-Id: 42
X-User-Role: LEARNER

Example: GET /api/v1/submissions/sub-456
```
Response: `200 OK` → SubmissionResponse (includes review data if reviewed)

---

### 2.3 Get All Submissions for an Assignment
```
GET /api/v1/submissions/assignments/{assignmentId}
X-User-Id: 1
X-User-Role: INSTRUCTOR

Example: GET /api/v1/submissions/assignments/abc-123
```
Response: `200 OK` → List<SubmissionResponse>

---

### 2.4 Get All Submissions by a Student
```
GET /api/v1/submissions/students/{studentId}
X-User-Id: 1
X-User-Role: INSTRUCTOR

Example: GET /api/v1/submissions/students/42
```
Response: `200 OK` → List<SubmissionResponse>

---

### 2.5 Get Specific Student's Submission for an Assignment
```
GET /api/v1/submissions/assignments/{assignmentId}/students/{studentId}
X-User-Id: 1
X-User-Role: INSTRUCTOR

Example: GET /api/v1/submissions/assignments/abc-123/students/42
```
Response: `200 OK` → SubmissionResponse

---

## 3. Reviews — `/api/v1/reviews`

### 3.1 Review (Grade) a Submission
```
POST /api/v1/reviews
Content-Type: application/json
X-User-Id: 1
X-User-Role: INSTRUCTOR

{
  "submissionId": "sub-456",
  "marksAwarded": 87.5,
  "feedback": "Excellent work! Clean code and good documentation."
}
```
Response: `201 Created` → ReviewResponse

Note: PASS/FAIL is auto-calculated (marksAwarded >= passMarks → PASS).
Submission status is updated to REVIEWED automatically.

---

### 3.2 Get Review for a Submission
```
GET /api/v1/reviews/submissions/{submissionId}
X-User-Id: 42
X-User-Role: LEARNER

Example: GET /api/v1/reviews/submissions/sub-456
```
Response: `200 OK` → ReviewResponse

---

### 3.3 Get All Reviews by a Reviewer
```
GET /api/v1/reviews/reviewers/{reviewerId}
X-User-Id: 1
X-User-Role: INSTRUCTOR

Example: GET /api/v1/reviews/reviewers/1
```
Response: `200 OK` → List<ReviewResponse>

---

### 3.4 Update a Review
```
PUT /api/v1/reviews/{reviewId}
Content-Type: application/json
X-User-Id: 1
X-User-Role: INSTRUCTOR

{
  "submissionId": "sub-456",
  "marksAwarded": 92.0,
  "feedback": "Updated feedback after re-evaluation."
}
```
Response: `200 OK` → ReviewResponse

---

## 4. Reports — `/api/v1/reports`

### 4.1 Get Report for an Assignment
```
GET /api/v1/reports/assignments/{assignmentId}
X-User-Id: 1
X-User-Role: INSTRUCTOR

Example: GET /api/v1/reports/assignments/abc-123
```
Response: `200 OK` → ReportResponse
```json
{
  "assignmentId": "abc-123",
  "assignmentTitle": "Java Collections Framework",
  "dueDate": "2026-07-30",
  "status": "PUBLISHED",
  "totalStudents": 140,
  "submittedCount": 98,
  "pendingCount": 42,
  "gradedCount": 75,
  "averageScore": 78.5,
  "highestScore": 100.0,
  "lowestScore": 22.0,
  "passCount": 60,
  "failCount": 15,
  "submissions": [...]
}
```

---

### 4.2 Get Report for All Assignments in a Course
```
GET /api/v1/reports/courses/{courseId}
X-User-Id: 1
X-User-Role: INSTRUCTOR

Example: GET /api/v1/reports/courses/101
```
Response: `200 OK` → List<ReportResponse>

---

### 4.3 Export Submissions as CSV
```
GET /api/v1/reports/assignments/{assignmentId}/export
X-User-Id: 1
X-User-Role: INSTRUCTOR

Example: GET /api/v1/reports/assignments/abc-123/export
```
Response: `200 OK` → CSV file download
```
SubmissionId,StudentId,Status,ObtainedMarks,ResultStatus,SubmittedAt
sub-456,42,REVIEWED,87.5,PASS,2026-06-17T10:00:00
```

---

## 5. Dashboard — `/api/v1/dashboard`

### 5.1 Instructor Dashboard
```
GET /api/v1/dashboard/instructors/{instructorId}
X-User-Id: 1
X-User-Role: INSTRUCTOR

Example: GET /api/v1/dashboard/instructors/1
```
Response: `200 OK`
```json
{
  "totalAssignments": 6,
  "totalSubmissions": 120,
  "pendingReviews": 42,
  "gradedSubmissions": 78
}
```

---

### 5.2 Student Dashboard
```
GET /api/v1/dashboard/students/{studentId}
X-User-Id: 42
X-User-Role: LEARNER

Example: GET /api/v1/dashboard/students/42
```
Response: `200 OK`
```json
{
  "totalAssignments": 6,
  "totalSubmissions": 1,
  "submittedCount": 1,
  "reviewedCount": 2,
  "pendingOverdue": 3,
  "averageScore": 87.5,
  "passCount": 2,
  "failCount": 0
}
```

---

### 5.3 Course Dashboard
```
GET /api/v1/dashboard/courses/{courseId}
X-User-Id: 1
X-User-Role: INSTRUCTOR

Example: GET /api/v1/dashboard/courses/101
```
Response: `200 OK`
```json
{
  "totalAssignments": 6,
  "totalSubmissions": 120
}
```

---

## 6. Student Assignments — `/api/v1/students/{studentId}/assignments`

These endpoints power the student "My Assignments" page.

### 6.1 Get All My Assignments (All tab)
```
GET /api/v1/students/{studentId}/assignments
X-User-Id: 42
X-User-Role: LEARNER

Example: GET /api/v1/students/42/assignments
```
Response: `200 OK` → List<StudentAssignmentResponse>

---

### 6.2 Get My Assignments Filtered by Status (tab filter)
```
GET /api/v1/students/{studentId}/assignments?status={STATUS}
X-User-Id: 42
X-User-Role: LEARNER

Status values:
  NOT_SUBMITTED  → "Not Submitted" tab
  SUBMITTED      → "Submitted" tab
  REVIEWED       → "Reviewed" tab
  UNDER_REVIEW   → "Under Review" tab

Example: GET /api/v1/students/42/assignments?status=SUBMITTED
Example: GET /api/v1/students/42/assignments?status=NOT_SUBMITTED
Example: GET /api/v1/students/42/assignments?status=REVIEWED
```
Response: `200 OK` → List<StudentAssignmentResponse>

---

### 6.3 Get Assignment Detail (detail/submit page)
```
GET /api/v1/students/{studentId}/assignments/{assignmentId}
X-User-Id: 42
X-User-Role: LEARNER

Example: GET /api/v1/students/42/assignments/abc-123
```
Response: `200 OK` → StudentAssignmentResponse
```json
{
  "assignmentId": "abc-123",
  "title": "Java Collections Framework",
  "description": "Create a complete REST API...",
  "totalMarks": 100.0,
  "passMarks": 60.0,
  "difficultyLevel": "MEDIUM",
  "assignmentStatus": "PUBLISHED",
  "dueDate": "2026-06-15",
  "createdAt": "2025-12-28T00:00:00",
  "overdue": false,
  "assignmentFileUrl": "https://bucket.s3.amazonaws.com/assignments/abc.pdf",
  "assignmentFileName": "Assignment_REST_API_Requirements.pdf",
  "submissionId": "sub-456",
  "submissionStatus": "REVIEWED",
  "submittedAt": "2026-06-09T10:00:00",
  "submissionFileUrl": "https://bucket.s3.amazonaws.com/submissions/xyz.pdf",
  "submissionFileName": "Renu_LJK_CaseStudy_Ecommerce.pdf",
  "marksAwarded": 44.0,
  "scorePercentage": 88.0,
  "resultStatus": "PASS",
  "feedback": "Excellent work on demonstrating HashMap, TreeMap...",
  "reviewedAt": "2026-06-12T10:00:00"
}
```

---

## 7. Error Responses

All errors return this shape:
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Assignment not found with id: abc-123",
  "timestamp": "2026-06-17T10:00:00"
}
```

| HTTP Status | When |
|---|---|
| 400 | Invalid request data, validation failed, already submitted |
| 401 | Missing X-User-Id header |
| 403 | Role not allowed (e.g. LEARNER hitting INSTRUCTOR endpoint) |
| 404 | Assignment / Submission / Review not found |
| 500 | File upload failed, unexpected error |

---

## 8. Complete URL Summary

| Method | URL | Role | Description |
|--------|-----|------|-------------|
| POST | `/api/v1/assignments` | Any | Create assignment |
| GET | `/api/v1/assignments` | Any | Get all assignments |
| GET | `/api/v1/assignments/{id}` | Any | Get one assignment |
| GET | `/api/v1/assignments/courses/{courseId}` | Any | By course |
| GET | `/api/v1/assignments/instructors/{instructorId}` | Any | By instructor |
| PUT | `/api/v1/assignments/{id}` | Any | Update assignment |
| DELETE | `/api/v1/assignments/{id}` | Any | Delete assignment |
| POST | `/api/v1/submissions` | Any | Submit assignment |
| GET | `/api/v1/submissions/{id}` | Any | Get submission |
| GET | `/api/v1/submissions/assignments/{assignmentId}` | Any | By assignment |
| GET | `/api/v1/submissions/students/{studentId}` | Any | By student |
| GET | `/api/v1/submissions/assignments/{assignmentId}/students/{studentId}` | Any | Specific |
| POST | `/api/v1/reviews` | INSTRUCTOR/ADMIN | Grade submission |
| GET | `/api/v1/reviews/submissions/{submissionId}` | Any | Get review |
| GET | `/api/v1/reviews/reviewers/{reviewerId}` | INSTRUCTOR/ADMIN | By reviewer |
| PUT | `/api/v1/reviews/{reviewId}` | INSTRUCTOR/ADMIN | Update review |
| GET | `/api/v1/reports/assignments/{assignmentId}` | INSTRUCTOR/ADMIN | Assignment report |
| GET | `/api/v1/reports/courses/{courseId}` | INSTRUCTOR/ADMIN | Course report |
| GET | `/api/v1/reports/assignments/{assignmentId}/export` | INSTRUCTOR/ADMIN | CSV export |
| GET | `/api/v1/dashboard/instructors/{instructorId}` | Any | Instructor stats |
| GET | `/api/v1/dashboard/students/{studentId}` | Any | Student stats |
| GET | `/api/v1/dashboard/courses/{courseId}` | Any | Course stats |
| GET | `/api/v1/students/{studentId}/assignments` | Any | My assignments |
| GET | `/api/v1/students/{studentId}/assignments?status=X` | Any | Filtered |
| GET | `/api/v1/students/{studentId}/assignments/{assignmentId}` | Any | Detail page |
