# Assessment Service — Changes & Architecture Guide

## What Was Changed and Why

---

## 1. Marks Type: `Integer` → `Float`

**Files changed:** `Assignment`, `Review`, `CreateAssignmentRequest`, `UpdateAssignmentRequest`, `ReviewAssignmentRequest`, `AssignmentResponse`, `ReviewResponse`, `SubmissionResponse`, `ReportResponse`, `DashboardResponse`, all mappers and service impls.

**Why:** The Figma UI shows marks like `90%`, `75%`, `92%` — decimal scores need float precision. Using `Integer` would truncate values like `88.5`.

---

## 2. AssignmentResponse — Fully Populated

**Before:** Only had `assignmentId`, `title`, `courseId`, `status` (String).

**After:** Includes all fields — `description`, `courseName`, `totalMarks`, `passMarks`, `assignmentType`, `difficultyLevel`, `dueDate`, `assignmentFileUrl`, `createdBy`, `createdAt`, `updatedAt`.

**Why:** The Figma Edit Assignment popup shows all these fields pre-filled when editing.

---

## 3. AssignmentController — File Upload Support

**Before:** Plain JSON endpoints, wrong base URL (`/assignments` instead of `/api/v1/assignments`).

**After:** 
- Uses `Constants.ASSIGNMENT_BASE_URL` (`/api/v1/assignments`)
- `POST` and `PUT` now consume `multipart/form-data` — accepts both JSON request part and optional file
- Added `/course/{courseId}` and `/instructor/{instructorId}` filter endpoints

**Why:** The Figma Create/Edit Assignment popup has an "Assignment Document" upload area (PDF, DOC, DOCX, TXT, DWG, DWF).

---

## 4. AssignmentService — File Upload in Create/Update

`createAssignment(request, file)` and `updateAssignment(id, request, file)` — file is uploaded to S3 under the `assignments/` folder. On update, the old file is deleted from S3 before uploading the new one.

---

## 5. ReportResponse — Added `dueDate` and `status`

**Why:** The Figma Assignment Report screen shows Due Date and Status (Active/Deactivated) columns in the table.

---

## 6. DynamoDB Entity Annotations

Added `@DynamoDbBean` and `@DynamoDbPartitionKey` to `Assignment`, `Submission`, `Review`.

**Why:** Without these annotations the `TableSchema.fromBean()` call in the repositories throws a runtime error — DynamoDB Enhanced Client cannot map the class to a table schema.

---

## 7. Security Architecture

### How it works in a microservice setup

```
Client → API Gateway → Assessment Service (this)
             ↓
       Validates JWT
       Sets headers:
         X-User-Id: 123
         X-User-Role: INSTRUCTOR
             ↓
       Forwards request with headers + original Authorization header
```

The API Gateway is the single point of JWT validation. Each downstream microservice trusts the headers it receives.

### What was added

#### `HeaderAuthFilter` (new)
Reads `X-User-Id` and `X-User-Role` headers and builds a Spring `SecurityContext` from them.
This is what makes `@PreAuthorize("hasRole('INSTRUCTOR')")` work on controller methods.

```
Incoming request
  → HeaderAuthFilter reads X-User-Id + X-User-Role
  → Creates UsernamePasswordAuthenticationToken with ROLE_INSTRUCTOR
  → Sets it in SecurityContextHolder
  → Spring Security can now evaluate @PreAuthorize expressions
```

#### `FeignAuthInterceptor` (new)
When this service calls another service (CourseService, EnrollmentService, etc.) via Feign, it automatically forwards:
- `Authorization` header (the original JWT token)
- `X-User-Id` header
- `X-User-Role` header

This is needed so downstream services also know who the caller is.

#### `SecurityConfig` (fixed)
- Added `@EnableMethodSecurity` — enables `@PreAuthorize` on controller methods
- Set session to `STATELESS` — no server-side sessions, each request is independent
- Registered `HeaderAuthFilter` before Spring's default auth filter
- Swagger/OpenAPI endpoints are public (no auth required)
- Everything else requires authentication

#### `CommonUtil` (improved)
- `extractUserIdFromRequest()` now checks `SecurityContextHolder` first (set by `HeaderAuthFilter`), then falls back to the raw header
- `extractUserRoleFromRequest()` same pattern — SecurityContext first, then header

### Why not validate JWT in this service?

In a microservice architecture, validating JWT in every single service is redundant and creates tight coupling — every service would need the same secret key and validation logic. The standard pattern is:

- **Gateway**: validates JWT, extracts claims, sets trusted headers
- **Microservices**: trust those headers, focus on business logic

If you want JWT validation here too (e.g., for direct access without a gateway), you'd implement it in `JwtUtil` and use it in `HeaderAuthFilter` to verify the `Authorization` header before trusting the `X-User-Id` header.

---

## 8. pom.xml — Added Dependencies

| Dependency | Why |
|---|---|
| `spring-boot-starter-security` | Was missing — required for `SecurityFilterChain`, `@PreAuthorize`, etc. |
| `dynamodb-enhanced` (AWS SDK v2) | Required by repositories using `DynamoDbEnhancedClient` / `TableSchema.fromBean()` |
| `springdoc-openapi-starter-webmvc-ui` | Swagger UI at `/swagger-ui.html` |

---

## File Map

```
security/
  SecurityConfig.java         ← Spring Security filter chain config
  HeaderAuthFilter.java       ← NEW: reads X-User-Id/X-User-Role → SecurityContext
  FeignAuthInterceptor.java   ← NEW: forwards auth headers to Feign calls
  JwtUtil.java                ← Placeholder (JWT handled by Gateway)
  JwtAuthenticationFilter.java← Placeholder (not used)
  UserPrincipal.java          ← Placeholder (principal is plain String userId)

util/
  CommonUtil.java             ← Extracts userId/role from SecurityContext or headers
  Constants.java              ← URL constants and header name constants
```
