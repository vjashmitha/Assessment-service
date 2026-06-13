package org.assessment.entity;

import lombok.*;
import org.assessment.enums.ResultStatus;
import org.assessment.enums.SubmissionStatus;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@DynamoDbBean
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private String id;           // UUID as String (DynamoDB partition key)

    private String assignmentId;
    private String studentId;
    private String content;
    private String fileUrl;

    private String status;        // SubmissionStatus name
    private String resultStatus;  // ResultStatus name

    private Double obtainedMarks;

    private String submittedAt;
    private String createdAt;
    private String updatedAt;

    @DynamoDbPartitionKey
    public String getId() { return id; }

    // Enum helpers
    public SubmissionStatus getStatusEnum() {
        return status != null ? SubmissionStatus.valueOf(status) : null;
    }

    public void setStatusEnum(SubmissionStatus s) {
        this.status = s != null ? s.name() : null;
    }

    public ResultStatus getResultStatusEnum() {
        return resultStatus != null ? ResultStatus.valueOf(resultStatus) : null;
    }

    public void setResultStatusEnum(ResultStatus r) {
        this.resultStatus = r != null ? r.name() : null;
    }

    public void prePersist() {
        String now = LocalDateTime.now().format(FORMATTER);
        if (createdAt == null) {
            createdAt = now;
            submittedAt = now;
        }
        updatedAt = now;
    }
}
