package org.assessment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.assessment.enums.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
// Table: "assignments" | Partition key: assignmentId
public class Assignment {

    private String assignmentId;
    private String title;
    private String description;
    private String courseId;
    private String courseName;
    private Float totalMarks;
    private Float passMarks;
    private AssignmentType assignmentType;
    private DifficultyLevel difficultyLevel;
    private AssignmentStatus status;
    private LocalDate dueDate;
    private String assignmentFileUrl;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @DynamoDbPartitionKey
    public String getAssignmentId() { return assignmentId; }
    public void setAssignmentId(String assignmentId) { this.assignmentId = assignmentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public Float getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Float totalMarks) { this.totalMarks = totalMarks; }

    public Float getPassMarks() { return passMarks; }
    public void setPassMarks(Float passMarks) { this.passMarks = passMarks; }

    public AssignmentType getAssignmentType() { return assignmentType; }
    public void setAssignmentType(AssignmentType assignmentType) { this.assignmentType = assignmentType; }

    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getAssignmentFileUrl() { return assignmentFileUrl; }
    public void setAssignmentFileUrl(String assignmentFileUrl) { this.assignmentFileUrl = assignmentFileUrl; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
