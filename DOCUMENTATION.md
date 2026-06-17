# Assessment Service — Complete Documentation

## Table of Contents
1. [What is this service?](#1-what-is-this-service)
2. [Project Structure](#2-project-structure)
3. [How a Request Flows](#3-how-a-request-flows)
4. [Security — How Authentication Works](#4-security--how-authentication-works)
5. [Database — DynamoDB Tables](#5-database--dynamodb-tables)
6. [File Storage — AWS S3](#6-file-storage--aws-s3)
7. [Feign Clients — Talking to Other Services](#7-feign-clients--talking-to-other-services)
8. [Entities — What Gets Stored](#8-entities--what-gets-stored)
9. [DTOs — What Gets Sent and Received](#9-dtos--what-gets-sent-and-received)
10. [API Endpoints — Full Reference](#10-api-endpoints--full-reference)
11. [Enums Reference](#11-enums-reference)
12. [Mappers — Entity ↔ DTO Conversion](#12-mappers--entity--dto-conversion)
13. [Utility Classes](#13-utility-classes)
14. [Configuration — application.yml](#14-configuration--applicationyml)
15. [Key Design Decisions Explained](#15-key-design-decisions-explained)

---

## 1. What is this service?

The **Assessment Service** is one microservice in a larger e-learning platform. Its only job is to manage:

- **Assignments** — instructors create them, students submit to them
- **Submissions** — students upload their work (file or text)
- **Reviews** — instructors grade submissions and leave feedback
- **Reports** — performance data per assignment or course
- **Dashboard** — summary statistics for instructors and students

It runs on **port 8083** and is registered with **Eureka** (service discovery), so other services can find it by name instead of hardcoded URLs.

---

## 2. Project Structure

```
src/main/java/org/assessment/
│
├── controller/          ← HTTP endpoints (what the outside world calls)
│   ├── AssignmentController.java
│   ├── SubmissionController.java
│   ├── ReviewController.java
│   ├── ReportController.java
│   └── DashboardController.java
│
├── service/             ← Business logic (the rules of the app)
│   ├── AssignmentService.java        (interface)
│   ├── SubmissionService.java        (interface)
│   ├── ReviewService.java            (interface)
│   ├── ReportService.java            (interface)
│   ├── DashboardService.java         (interface)
│   └── impl/
│       ├── AssignmentServiceImpl.java
│       ├── SubmissionServiceImpl.java
│       ├── ReviewServiceImpl.java
│       ├── ReportServiceImpl.java
│       └── DashboardServiceImpl.java
│
├── repository/          ← Data access (reads/writes to DynamoDB)
│   ├── AssignmentRepository.java
│   ├── SubmissionRepository.java
│   └── ReviewRepository.java
│
├── entity/              ← DynamoDB table models (what's stored in DB)
│   ├── Assignment.java
│   ├── Submission.java
│   └── Review.java
│
├── dto/                 ← Data Transfer Objects (what the API sends/receives)
│   ├── request/
│   │   ├── CreateAssignmentRequest.java
│   │   ├── UpdateAssignmentRequest.java
│   │   ├── SubmitAssignmentRequest.java
│   │   └── ReviewAssignmentRequest.java
│   └── response/
│       ├── AssignmentResponse.java
│       ├── SubmissionResponse.java
│       ├── ReviewResponse.java
│       ├── ReportResponse.java
│       └── DashboardResponse.java
│
├── mapper/              ← Converts entity ↔ DTO
│   ├── AssignmentMapper.java
│   ├── SubmissionMapper.java
│   └── AssignmentReviewMapper.java
│
├── security/            ← Who is allowed to do what
│   ├── SecurityConfig.java           (Spring Security rules)
│   ├── HeaderAuthFilter.java         (reads user identity from headers)
│   └── FeignAuthInterceptor.java     (forwards auth to other services)
│
├── client/              ← Feign clients (calls to other microservices)
│   ├── CourseServiceClient.java
│   ├── EnrollmentServiceClient.java
│   ├── UserServiceClient.java
│   └── NotificationServiceClient.java
│
├── storage/             ← AWS S3 file upload/download
│   ├── S3Service.java
│   └── S3ServiceImpl.java
│
├── config/              ← AWS and Swagger setup
│   ├── AwsS3Config.java
│   ├── DynamoDbConfig.java
│   └── SwaggerConfig.java
│
├── enums/               ← Fixed value lists
│   ├── AssignmentStatus.java
│   ├── AssignmentType.java
│   ├── DifficultyLevel.java
│   ├── ResultStatus.java
│   ├── Role.java
│   └── SubmissionStatus.java
│
├── exception/           ← Custom errors
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── ValidationException.java
│   └── FileUploadException.java
│
└── util/
    ├── CommonUtil.java   (helper: get current user from request)
    └── Constants.java    (URL paths and header names)
```

---

## 3. How a Request Flows

Let's trace what happens when a student submits an assignment:

```
Student's Browser / App
        │
        │  POST /api/v1/submissions
        │  Headers: Authorization: Bearer <jwt>
        │           X-User-Id: 42
        │           X-User-Role: LEARNER
        ▼
   API Gateway  (another service — not this one)
        │  Validates the JWT token
        │  Strips or keeps Authorization header
        │  Forwards X-User-Id and X-User-Role
        ▼
   Assessment Service (port 8083)
        │
        ├─ [1] HeaderAuthFilter runs first
        │       reads X-User-Id = "42"
        │       reads X-User-Role = "LEARNER"
        │       creates Authentication object → stored in SecurityContext
        │
        ├─ [2] SecurityConfig checks: is request authenticated? YES → pass
        │
        ├─ [3] SubmissionController.submitAssignment() is called
        │
        ├─ [4] SubmissionServiceImpl runs the logic:
        │       - gets studentId from CommonUtil (reads SecurityContext)
        │       - checks assignment exists in DynamoDB
        │       - calls EnrollmentServiceClient (Feign) to verify enrollment
        │           └─ FeignAuthInterceptor attaches auth headers to this call
        │       - checks student hasn't already submitted
        │       - uploads file to S3 (if provided)
        │       - saves Submission to DynamoDB
        │
        ├─ [5] SubmissionMapper converts Submission entity → SubmissionResponse
        │
        └─ [6] Returns HTTP 201 with SubmissionResponse JSON
```

---

## 4. Security — How Authentication Works

### The big picture

This service does **NOT** validate JWT tokens itself. That's the API Gateway's job. The gateway validates the token, then tells this service "trust me, this is user 42, they are an INSTRUCTOR" via HTTP headers.

```
JWT Validation happens HERE (Gateway)
         ↓
X-User-Id: 42          ← this service reads this
X-User-Role: INSTRUCTOR ← this service reads this
```

### HeaderAuthFilter.java

Every incoming HTTP request passes through this filter first.

```java
// What it does:
String userId   = request.getHeader("X-User-Id");    // e.g. "42"
String userRole = request.getHeader("X-User-Role");  // e.g. "INSTRUCTOR"

// Puts this into Spring Security's context:
// → Now @PreAuthorize("hasRole('INSTRUCTOR')") works
// → Now CommonUtil.extractUserIdFromRequest() returns "42"
```

If there's no `X-User-Id` header, the user is anonymous → Spring Security will block them (unless the endpoint is public).

### SecurityConfig.java

Defines the rules:

| Path | Access |
|------|--------|
| `/swagger-ui/**` | Public (no login needed) |
| `/api-docs/**` | Public |
| `/v3/api-docs/**` | Public |
| `/actuator/**` | Public |
| Everything else | Must be authenticated (needs X-User-Id header) |

Also has `@EnableMethodSecurity` which activates `@PreAuthorize` on controller methods — so `@PreAuthorize("hasRole('INSTRUCTOR')")` actually does something.

### FeignAuthInterceptor.java

When this service calls another service (e.g. checking if a student is enrolled), it uses Feign clients. This interceptor automatically attaches the auth headers to those outgoing calls.

```
Assessment Service calls EnrollmentService via Feign
      ↓
FeignAuthInterceptor kicks in automatically
      ↓
Adds to outgoing request:
  Authorization: Bearer <original jwt>
  X-User-Id: 42
  X-User-Role: INSTRUCTOR
```

Without this, the downstream service would reject the request because it wouldn't know who is calling.

### CommonUtil.java

A helper that services use to get the current user's ID:

```java
String userId = CommonUtil.extractUserIdFromRequest();
// Returns "42" — set by HeaderAuthFilter from the X-User-Id header
```

It first checks the Spring `SecurityContextHolder` (set by `HeaderAuthFilter`), then falls back to reading the raw header directly.

---

## 5. Database — DynamoDB Tables

Three tables, each configured in `application.yml`:

### assignments table
Partition key: `assignmentId` (UUID string)

| Field | Type | Description |
|-------|------|-------------|
| assignmentId | String | UUID, primary key |
| title | String | Assignment name |
| description | String | What students need to do |
| courseId | String | Which course this belongs to |
| courseName | String | Display name of the course |
| totalMarks | Float | Max marks possible (e.g. 100.0) |
| passMarks | Float | Minimum to pass (e.g. 40.0) |
| assignmentType | Enum | FILE_UPLOAD / QUIZ / PROJECT |
| difficultyLevel | Enum | BEGINNER / INTERMEDIATE / ADVANCED |
| status | Enum | DRAFT / PUBLISHED / CLOSED |
| dueDate | LocalDate | Deadline |
| assignmentFileUrl | String | S3 URL of the assignment PDF/doc |
| createdBy | String | Instructor's userId |
| createdAt | LocalDateTime | When created |
| updatedAt | LocalDateTime | Last modified |

### submissions table
Partition key: `submissionId` (UUID string)

| Field | Type | Description |
|-------|------|-------------|
| submissionId | String | UUID, primary key |
| assignmentId | String | Links to assignments table |
| learnerId | String | Student's userId |
| submissionFileUrl | String | S3 URL of student's uploaded file |
| status | Enum | SUBMITTED / UNDER_REVIEW / REVIEWED |
| submittedAt | LocalDateTime | When student submitted |

### assignment-reviews table
Partition key: `reviewId` (UUID string)

| Field | Type | Description |
|-------|------|-------------|
| reviewId | String | UUID, primary key |
| submissionId | String | Links to submissions table |
| reviewerId | String | Instructor's userId |
| marksAwarded | Float | Score given (e.g. 87.5) |
| feedback | String | Instructor's written feedback |
| resultStatus | Enum | PASS / FAIL / PENDING |
| reviewedAt | LocalDateTime | When reviewed |

### Why DynamoDB?

DynamoDB is AWS's NoSQL database. It's schema-less (no fixed columns), scales automatically, and has no server to manage. The `@DynamoDbBean` annotation tells the AWS SDK this class maps to a DynamoDB table, and `@DynamoDbPartitionKey` marks the primary key.

> **Note:** The repositories use `table.scan()` with in-memory filtering. This works fine for small datasets but will be slow at scale. For production, add GSIs (Global Secondary Indexes) for fields you query often (courseId, learnerId, assignmentId).

---

## 6. File Storage — AWS S3

Used for two things:
- **Assignment documents** — PDFs/DOCs uploaded by instructors when creating/editing assignments
- **Submission files** — files uploaded by students when submitting

### Upload path structure in S3
```
assignments/<uuid>.<ext>   ← instructor uploads
submissions/<uuid>.<ext>   ← student uploads
```

### S3Service methods

| Method | What it does |
|--------|-------------|
| `uploadFile(file, folder)` | Uploads to S3, returns the public URL |
| `deleteFile(fileUrl)` | Deletes from S3 using the URL |
| `getFileUrl(key)` | Builds the full HTTPS S3 URL from a key |

When an assignment is updated with a new file, the old file is deleted first to avoid orphaned files.

---

## 7. Feign Clients — Talking to Other Services

Feign is a library that lets you call another service's REST API as if it were a local Java method. No manual HTTP calls needed.

### CourseServiceClient
```java
// Checks if a course exists before creating an assignment
Boolean exists = courseServiceClient.courseExists(courseId);
```
- Called in: `AssignmentServiceImpl.createAssignment()`
- URL: `${services.course-service}/api/v1/courses/{courseId}/exists`

### EnrollmentServiceClient
```java
// Checks if a student is enrolled in the course before allowing submission
Boolean enrolled = enrollmentServiceClient.isEnrolled(studentId, courseId);
```
- Called in: `SubmissionServiceImpl.submitAssignment()`
- URL: `${services.enrollment-service}/api/v1/enrollments/check`

### UserServiceClient
```java
// Checks if a user ID actually exists
Boolean exists = userServiceClient.userExists(userId);
```
- URL: `${services.user-service}/api/v1/users/{userId}/exists`

### NotificationServiceClient
```java
// Sends a notification (e.g. "your submission was graded")
notificationServiceClient.sendNotification(payload);
```
- URL: `${services.notification-service}/api/v1/notifications/send`

All Feign calls automatically get the auth headers attached via `FeignAuthInterceptor`.

---

## 8. Entities — What Gets Stored

Entities are the Java classes that map directly to DynamoDB table rows.

### Why explicit getters/setters instead of Lombok @Getter/@Setter?

DynamoDB Enhanced Client uses `@DynamoDbPartitionKey` on the **getter method**. If we use Lombok `@Getter`, the annotation would need to go on the field, but DynamoDB needs it on the method. So for entities, we write explicit getters/setters and put `@DynamoDbPartitionKey` on the `getXxx()` method.

```java
@DynamoDbPartitionKey          // ← must be on the getter
public String getAssignmentId() { return assignmentId; }
```

We still use `@NoArgsConstructor`, `@AllArgsConstructor`, and `@Builder` from Lombok since those work on class level and don't conflict.

---

## 9. DTOs — What Gets Sent and Received

DTOs (Data Transfer Objects) are what the API exposes. They are separate from entities so you control exactly what data enters and leaves the service.

### Request DTOs (what clients send to you)

**CreateAssignmentRequest** — used when creating a new assignment
```
title*         String        Assignment title
description    String        What students need to do
courseId*      String        Which course (validated against CourseService)
totalMarks*    Float         Max marks (e.g. 100.0)
passMarks*     Float         Minimum to pass (e.g. 40.0)
assignmentType AssignmentType FILE_UPLOAD / QUIZ / PROJECT
difficultyLevel DifficultyLevel BEGINNER / INTERMEDIATE / ADVANCED
dueDate        LocalDate     Deadline (yyyy-MM-dd)
```

**UpdateAssignmentRequest** — all fields optional (only send what you want to change)
```
title          String
description    String
assignmentType AssignmentType
difficultyLevel DifficultyLevel
status         AssignmentStatus  DRAFT / PUBLISHED / CLOSED
totalMarks     Float
passMarks      Float
dueDate        LocalDate
allowLateSubmission Boolean
```

**SubmitAssignmentRequest** — used when a student submits
```
assignmentId*  String   Which assignment to submit to
content        String   Text content (for text-based submissions)
fileUrl        String   Optional: pre-uploaded file URL
```
Note: The file itself is sent as a separate multipart field, not inside this JSON.

**ReviewAssignmentRequest** — used when instructor grades a submission
```
submissionId*  String   Which submission to review
feedback       String   Written comments
marksAwarded*  Float    Score (e.g. 87.5)
```

### Response DTOs (what you return to clients)

**AssignmentResponse** — full assignment details
```
assignmentId, title, description, courseId, courseName,
totalMarks, passMarks, assignmentType, difficultyLevel,
status, dueDate, assignmentFileUrl, createdBy, createdAt, updatedAt
```

**SubmissionResponse** — submission with review info merged in
```
id, assignmentId, studentId, studentName, content, fileUrl,
status, resultStatus, obtainedMarks, submittedAt, createdAt, updatedAt
```

**ReviewResponse** — review/grading details
```
id, submissionId, reviewerId, feedback,
marksAwarded, resultStatus, reviewedAt, createdAt, updatedAt
```

**ReportResponse** — per-assignment performance stats
```
assignmentId, assignmentTitle, dueDate, status,
totalStudents, submittedCount, pendingCount, gradedCount,
averageScore, highestScore, lowestScore, passCount, failCount,
submissions (list of SubmissionResponse)
```

**DashboardResponse** — summary numbers for a person
```
totalAssignments, totalSubmissions, pendingReviews,
gradedSubmissions, averageScore, passCount, failCount
```

---

## 10. API Endpoints — Full Reference

Base URL: `http://localhost:8083`

### Assignments — `/api/v1/assignments`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/assignments` | Any authenticated | Create assignment + optional file upload |
| GET | `/api/v1/assignments` | Any authenticated | Get all assignments |
| GET | `/api/v1/assignments/{id}` | Any authenticated | Get one assignment by ID |
| GET | `/api/v1/assignments/course/{courseId}` | Any authenticated | Get all assignments for a course |
| GET | `/api/v1/assignments/instructor/{instructorId}` | Any authenticated | Get assignments created by instructor |
| PUT | `/api/v1/assignments/{id}` | Any authenticated | Update assignment + optional new file |
| DELETE | `/api/v1/assignments/{id}` | Any authenticated | Delete assignment |

**Create/Update uses multipart/form-data:**
```
Part "request": JSON (CreateAssignmentRequest or UpdateAssignmentRequest)
Part "file":    PDF/DOC file (optional)
```

**Example create request:**
```
POST /api/v1/assignments
Content-Type: multipart/form-data

--boundary
Content-Disposition: form-data; name="request"
Content-Type: application/json

{
  "title": "Java Collections Assignment",
  "courseId": "101",
  "totalMarks": 100.0,
  "passMarks": 40.0,
  "assignmentType": "FILE_UPLOAD",
  "difficultyLevel": "INTERMEDIATE",
  "dueDate": "2026-07-30"
}
--boundary
Content-Disposition: form-data; name="file"; filename="assignment.pdf"
Content-Type: application/pdf

<binary file data>
```

---

### Submissions — `/api/v1/submissions`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/submissions` | Any authenticated | Submit assignment (studentId taken from X-User-Id header) |
| GET | `/api/v1/submissions/{id}` | Any authenticated | Get submission by ID |
| GET | `/api/v1/submissions/assignment/{assignmentId}` | Any authenticated | All submissions for an assignment |
| GET | `/api/v1/submissions/student/{studentId}` | Any authenticated | All submissions by a student |
| GET | `/api/v1/submissions/assignment/{assignmentId}/student/{studentId}` | Any authenticated | Specific student's submission for specific assignment |

**Submit uses multipart/form-data:**
```
Part "request": JSON (SubmitAssignmentRequest)
Part "file":    student's work file (optional)
```

**Validations that happen on submit:**
1. Assignment must exist
2. Assignment status must be PUBLISHED (not DRAFT or CLOSED)
3. Student must be enrolled in the course (checked via EnrollmentService)
4. Student must not have already submitted (one submission per student per assignment)

---

### Reviews — `/api/v1/reviews`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/reviews` | INSTRUCTOR or ADMIN only | Grade a submission |
| GET | `/api/v1/reviews/submission/{submissionId}` | Any authenticated | Get review for a submission |
| GET | `/api/v1/reviews/reviewer/{reviewerId}` | INSTRUCTOR or ADMIN only | All reviews by a reviewer |
| PUT | `/api/v1/reviews/{reviewId}` | INSTRUCTOR or ADMIN only | Update a review |

**On review creation:**
- Checks if submission was already reviewed (no double grading)
- Auto-calculates PASS/FAIL based on `marksAwarded >= passMarks`
- Updates submission status to REVIEWED

**Example body:**
```json
{
  "submissionId": "abc-123",
  "marksAwarded": 87.5,
  "feedback": "Excellent work! Clean code and good documentation."
}
```

---

### Reports — `/api/v1/reports`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/v1/reports/assignment/{assignmentId}` | INSTRUCTOR or ADMIN | Full report for one assignment |
| GET | `/api/v1/reports/course/{courseId}` | INSTRUCTOR or ADMIN | Reports for all assignments in a course |
| GET | `/api/v1/reports/assignment/{assignmentId}/export` | INSTRUCTOR or ADMIN | Download CSV of submissions |

**ReportResponse includes:**
- Stats: total students, submitted count, pending, graded, average/highest/lowest score
- Full list of submissions with their review data

---

### Dashboard — `/api/v1/dashboard`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/v1/dashboard/instructor/{instructorId}` | Any authenticated | Instructor's overview stats |
| GET | `/api/v1/dashboard/student/{studentId}` | Any authenticated | Student's progress stats |
| GET | `/api/v1/dashboard/course/{courseId}` | Any authenticated | Course-level assignment stats |

**Instructor dashboard returns:**
- totalAssignments they created
- totalSubmissions received across all their assignments
- pendingReviews (submissions not yet graded)
- gradedSubmissions

**Student dashboard returns:**
- totalSubmissions they made
- gradedSubmissions (reviewed ones)
- passCount / failCount
- averageScore

---

## 11. Enums Reference

### AssignmentStatus
```
DRAFT      → Created but not visible to students yet
PUBLISHED  → Open for student submissions
CLOSED     → No more submissions accepted
```

### AssignmentType
```
FILE_UPLOAD → Students upload a file (PDF, DOC, etc.)
QUIZ        → Quiz-based assignment
PROJECT     → Project submission
```

### DifficultyLevel
```
BEGINNER
INTERMEDIATE
ADVANCED
```

### SubmissionStatus
```
NOT_SUBMITTED  → Student hasn't submitted yet (default state)
SUBMITTED      → File/content uploaded, waiting for review
UNDER_REVIEW   → Instructor is reviewing
REVIEWED       → Graded and feedback given
```

### ResultStatus
```
PASS     → marksAwarded >= passMarks
FAIL     → marksAwarded < passMarks
PENDING  → Not yet reviewed
```

### Role
```
ADMIN
TRAINER
LEARNER
```

---

## 12. Mappers — Entity ↔ DTO Conversion

Mappers convert between the database model (entity) and the API model (DTO). This separation is intentional — you might store more in the DB than you want to expose in the API, or the API shape might be different from the storage shape.

### AssignmentMapper
- `toEntity(CreateAssignmentRequest, instructorId)` — creates a new Assignment with a UUID, sets status to DRAFT, sets timestamps
- `toResponse(Assignment)` — returns the full AssignmentResponse including all fields

### SubmissionMapper
- `toEntity(SubmitAssignmentRequest, learnerId)` — creates a new Submission, sets status to SUBMITTED
- `toResponse(Submission)` — response without review data
- `toResponse(Submission, Review)` — response with review data merged in (obtainedMarks, resultStatus)

### AssignmentReviewMapper
- `toEntity(ReviewAssignmentRequest, reviewerId)` — creates a new Review with timestamp
- `toResponse(Review)` — returns ReviewResponse

---

## 13. Utility Classes

### Constants.java
All string constants in one place so nothing is hardcoded elsewhere.

```java
API_VERSION       = "/api/v1"
ASSIGNMENT_BASE_URL = "/api/v1/assignments"
SUBMISSION_BASE_URL = "/api/v1/submissions"
REVIEW_BASE_URL     = "/api/v1/reviews"
DASHBOARD_BASE_URL  = "/api/v1/dashboard"
REPORT_BASE_URL     = "/api/v1/reports"

AUTHORIZATION_HEADER = "Authorization"
USER_ID_HEADER       = "X-User-Id"
USER_ROLE_HEADER     = "X-User-Role"
BEARER_PREFIX        = "Bearer "
```

### CommonUtil.java
Gets the current user's information from the active request.

```java
// Get the logged-in user's ID (used by services to know who is acting)
String userId = CommonUtil.extractUserIdFromRequest();

// Get the logged-in user's role
String role = CommonUtil.extractUserRoleFromRequest();

// Get the raw JWT token (if needed to pass somewhere)
String token = CommonUtil.extractTokenFromRequest();
```

These are `static` methods, so you call them without injecting anything. They first check the `SecurityContextHolder` (set by `HeaderAuthFilter`) and fall back to reading raw headers if needed.

---

## 14. Configuration — application.yml

```yaml
server:
  port: 8083              ← This service runs on port 8083

aws:
  region: us-east-1       ← AWS region for DynamoDB and S3
  access-key: xxx         ← AWS credentials (use IAM roles in production, not plain text)
  secret-key: xxx
  s3:
    bucket-name: assessment-files   ← S3 bucket for uploaded files

dynamodb:
  table:
    assignments: assignments        ← DynamoDB table names
    submissions: submissions
    reviews: assignment-reviews

services:                           ← URLs of other microservices (for Feign)
  user-service: http://localhost:8081
  course-service: http://localhost:8082
  enrollment-service: http://localhost:8084
  notification-service: http://localhost:8085

feign:
  client:
    config:
      default:
        connectTimeout: 5000        ← 5 seconds to connect to another service
        readTimeout: 5000           ← 5 seconds to wait for response
```

---

## 15. Key Design Decisions Explained

### Why is marks Float and not Integer?

The Figma UI shows scores like `87.5`, `92.3`. Float supports decimals. Integer would round off `87.5` to `87` which loses precision. Float is the right choice for any scoring/grading system.

### Why separate Entity and DTO?

Entity = what's in the database. DTO = what the API exposes.
- The entity might have internal fields (createdBy, updatedAt) you don't always want to expose
- The DTO can combine data from multiple entities (SubmissionResponse includes review data)
- Changing the API shape doesn't require changing the database schema

### Why no JWT validation in this service?

In a microservice setup, doing JWT validation in every single service means:
1. Every service needs the same secret key
2. Every service has the same JWT validation code duplicated
3. If you change your auth system, you update every service

The standard pattern: API Gateway validates JWT once → extracts user info → forwards it as trusted headers. Each downstream service trusts those headers. This service follows that pattern.

### Why use DynamoDB?

DynamoDB suits this service because:
- Assignments and submissions are accessed by ID most of the time (DynamoDB is fast for key-based access)
- No complex joins needed between tables
- Scales automatically as the platform grows
- Fully managed — no database server maintenance

### Why multipart/form-data for create/update assignment?

The Figma UI has a file upload section in the Create/Edit Assignment form. To send both JSON data (title, marks, etc.) and a binary file in one request, multipart/form-data is the standard approach — one part is the JSON, another part is the file.

### Why @PreAuthorize only on review/report endpoints?

- **Assignments** — instructors create them, but students also need to read them. So no role restriction on reads.
- **Submissions** — students submit, instructors read. Mixed roles, so we let business logic decide.
- **Reviews** — only INSTRUCTOR or ADMIN should grade. Hard restriction via `@PreAuthorize`.
- **Reports** — sensitive analytics, only INSTRUCTOR or ADMIN.
