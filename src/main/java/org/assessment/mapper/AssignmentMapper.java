package org.assessment.mapper;

import org.assessment.dto.request.CreateAssignmentRequest;
import org.assessment.dto.response.AssignmentResponse;
import org.assessment.entity.Assignment;
import org.assessment.enums.AssignmentStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AssignmentMapper {

    public Assignment toEntity(CreateAssignmentRequest request, String instructorId) {
        Assignment assignment = new Assignment();
        assignment.setId(UUID.randomUUID().toString());
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setCourseId(request.getCourseId());
        assignment.setInstructorId(instructorId);
        assignment.setAssignmentTypeEnum(request.getAssignmentType());
        assignment.setDifficultyLevelEnum(request.getDifficultyLevel());
        assignment.setStatusEnum(AssignmentStatus.DRAFT);
        assignment.setTotalMarks(request.getTotalMarks());
        assignment.setPassingMarks(request.getPassingMarks());
        assignment.setAllowLateSubmission(request.getAllowLateSubmission());
        if (request.getDueDate() != null) {
            assignment.setDueDateFromLocalDateTime(request.getDueDate());
        }
        return assignment;
    }

    public AssignmentResponse toResponse(Assignment assignment) {
        return AssignmentResponse.builder()
                .id(assignment.getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .courseId(assignment.getCourseId())
                .instructorId(assignment.getInstructorId())
                .assignmentType(assignment.getAssignmentTypeEnum())
                .difficultyLevel(assignment.getDifficultyLevelEnum())
                .status(assignment.getStatusEnum())
                .totalMarks(assignment.getTotalMarks())
                .passingMarks(assignment.getPassingMarks())
                .dueDate(assignment.getDueDateAsLocalDateTime())
                .allowLateSubmission(assignment.getAllowLateSubmission())
                .attachmentUrl(assignment.getAttachmentUrl())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }
}
