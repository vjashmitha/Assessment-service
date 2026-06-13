package org.assessment.service.impl;

import lombok.RequiredArgsConstructor;
import org.assessment.dto.request.CreateAssignmentRequest;
import org.assessment.dto.request.UpdateAssignmentRequest;
import org.assessment.dto.response.AssignmentResponse;
import org.assessment.entity.Assignment;
import org.assessment.enums.AssignmentStatus;
import org.assessment.exception.ResourceNotFoundException;
import org.assessment.exception.ValidationException;
import org.assessment.mapper.AssignmentMapper;
import org.assessment.repository.AssignmentRepository;
import org.assessment.service.AssignmentService;
import org.assessment.storage.S3Service;
import org.assessment.util.CommonUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentMapper assignmentMapper;
    private final S3Service s3Service;

    @Override
    public AssignmentResponse createAssignment(CreateAssignmentRequest request, MultipartFile attachment) {
        String instructorId = CommonUtil.extractUserIdFromRequest();
        Assignment assignment = assignmentMapper.toEntity(request, instructorId);
        if (attachment != null && !attachment.isEmpty()) {
            assignment.setAttachmentUrl(s3Service.uploadFile(attachment, "assignments"));
        }
        return assignmentMapper.toResponse(assignmentRepository.save(assignment));
    }

    @Override
    public AssignmentResponse getAssignmentById(String id) {
        return assignmentMapper.toResponse(findOrThrow(id));
    }

    @Override
    public List<AssignmentResponse> getAssignmentsByCourse(String courseId) {
        return assignmentRepository.findByCourseId(courseId)
                .stream().map(assignmentMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<AssignmentResponse> getAssignmentsByInstructor(String instructorId) {
        return assignmentRepository.findByInstructorId(instructorId)
                .stream().map(assignmentMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public AssignmentResponse updateAssignment(String id, UpdateAssignmentRequest request) {
        Assignment assignment = findOrThrow(id);
        if (request.getTitle() != null) assignment.setTitle(request.getTitle());
        if (request.getDescription() != null) assignment.setDescription(request.getDescription());
        if (request.getAssignmentType() != null) assignment.setAssignmentTypeEnum(request.getAssignmentType());
        if (request.getDifficultyLevel() != null) assignment.setDifficultyLevelEnum(request.getDifficultyLevel());
        if (request.getStatus() != null) assignment.setStatusEnum(request.getStatus());
        if (request.getTotalMarks() != null) assignment.setTotalMarks(request.getTotalMarks());
        if (request.getPassingMarks() != null) assignment.setPassingMarks(request.getPassingMarks());
        if (request.getDueDate() != null) assignment.setDueDateFromLocalDateTime(request.getDueDate());
        if (request.getAllowLateSubmission() != null) assignment.setAllowLateSubmission(request.getAllowLateSubmission());
        return assignmentMapper.toResponse(assignmentRepository.save(assignment));
    }

    @Override
    public void deleteAssignment(String id) {
        Assignment assignment = findOrThrow(id);
        if (assignment.getAttachmentUrl() != null) {
            s3Service.deleteFile(assignment.getAttachmentUrl());
        }
        assignmentRepository.delete(assignment);
    }

    @Override
    public AssignmentResponse publishAssignment(String id) {
        Assignment assignment = findOrThrow(id);
        if (assignment.getStatusEnum() != AssignmentStatus.DRAFT) {
            throw new ValidationException("Only DRAFT assignments can be published");
        }
        assignment.setStatusEnum(AssignmentStatus.PUBLISHED);
        return assignmentMapper.toResponse(assignmentRepository.save(assignment));
    }

    private Assignment findOrThrow(String id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + id));
    }
}
