package org.assessment.entity;

import lombok.*;
import org.assessment.enums.AssignmentStatus;
import org.assessment.enums.AssignmentType;
import org.assessment.enums.DifficultyLevel;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@DynamoDbBean
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private String id;           // UUID as String (DynamoDB partition key)

    private String title;
    private String description;
    private String courseId;
    private String instructorId;

    private String assignmentType;    // stored as String (enum name)
    private String difficultyLevel;
    private String status;

    private Double totalMarks;
    private Double passingMarks;
    private String dueDate;           // ISO string — LocalDateTime not natively supported by DynamoDB mapper

    private Boolean allowLateSubmission;
    private String attachmentUrl;

    private String createdAt;
    private String updatedAt;

    @DynamoDbPartitionKey
    public String getId() { return id; }

    // Convenience helpers for enum/datetime conversion
    public AssignmentType getAssignmentTypeEnum() {
        return assignmentType != null ? AssignmentType.valueOf(assignmentType) : null;
    }

    public void setAssignmentTypeEnum(AssignmentType type) {
        this.assignmentType = type != null ? type.name() : null;
    }

    public AssignmentStatus getStatusEnum() {
        return status != null ? AssignmentStatus.valueOf(status) : null;
    }

    public void setStatusEnum(AssignmentStatus s) {
        this.status = s != null ? s.name() : null;
    }

    public DifficultyLevel getDifficultyLevelEnum() {
        return difficultyLevel != null ? DifficultyLevel.valueOf(difficultyLevel) : null;
    }

    public void setDifficultyLevelEnum(DifficultyLevel level) {
        this.difficultyLevel = level != null ? level.name() : null;
    }

    public LocalDateTime getDueDateAsLocalDateTime() {
        return dueDate != null ? LocalDateTime.parse(dueDate, FORMATTER) : null;
    }

    public void setDueDateFromLocalDateTime(LocalDateTime dt) {
        this.dueDate = dt != null ? dt.format(FORMATTER) : null;
    }

    public void prePersist() {
        String now = LocalDateTime.now().format(FORMATTER);
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }
}
