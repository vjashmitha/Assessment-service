package org.assessment.mapper;

import org.assessment.dto.request.CreateAssignmentRequest;
import org.assessment.dto.response.AssignmentResponse;
import org.assessment.entity.Assignment;
import org.assessment.enums.AssignmentStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class AssignmentMapper {

	public Assignment toEntity(CreateAssignmentRequest request, String instructorId) {
		if (request == null) {
			return null;
		}
		Assignment assignment = new Assignment();
		assignment.setAssignmentId(UUID.randomUUID().toString());
		assignment.setTitle(request.getTitle());
		assignment.setDescription(request.getDescription());
		assignment.setCourseId(request.getCourseId());
		assignment.setCreatedBy(instructorId);
		assignment.setAssignmentType(request.getAssignmentType());
		assignment.setDifficultyLevel(request.getDifficultyLevel());
		assignment.setStatus(AssignmentStatus.DRAFT);
		assignment.setTotalMarks(request.getTotalMarks());
		assignment.setPassMarks(request.getPassMarks());
		assignment.setCreatedAt(LocalDateTime.now().toString());
		assignment.setUpdatedAt(LocalDateTime.now().toString());
		assignment.setDueDate(request.getDueDate() != null ? request.getDueDate().toString() : null);
		return assignment;
	}

	public AssignmentResponse toResponse(Assignment assignment) {
		if (assignment == null) {
			return null;
		}
		return AssignmentResponse.builder().assignmentId(assignment.getAssignmentId()).title(assignment.getTitle())
				.description(assignment.getDescription()).courseId(assignment.getCourseId())
				.courseName(assignment.getCourseName()).totalMarks(assignment.getTotalMarks())
				.passMarks(assignment.getPassMarks()).assignmentType(assignment.getAssignmentType())
				.difficultyLevel(assignment.getDifficultyLevel()).status(assignment.getStatus())
				.dueDate(assignment.getDueDate() != null ? assignment.getDueDate().toString() : null)
				.assignmentFileUrl(assignment.getAssignmentFileUrl()).createdBy(assignment.getCreatedBy())
				.createdAt(assignment.getCreatedAt() != null ? assignment.getCreatedAt().toString() : null)
				.updatedAt(assignment.getUpdatedAt() != null ? assignment.getUpdatedAt().toString() : null).build();
	}
}
