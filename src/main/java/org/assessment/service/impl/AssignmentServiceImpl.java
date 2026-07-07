package org.assessment.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.assessment.client.CourseServiceClient;
import org.assessment.dto.request.CreateAssignmentRequest;
import org.assessment.dto.request.UpdateAssignmentRequest;
import org.assessment.dto.response.AssignmentResponse;
import org.assessment.dto.response.CourseResponse;
import org.assessment.entity.Assignment;
import org.assessment.exception.ResourceNotFoundException;
import org.assessment.exception.ValidationException;
import org.assessment.mapper.AssignmentMapper;
import org.assessment.repository.AssignmentRepository;
import org.assessment.service.AssignmentService;
import org.assessment.storage.S3Service;
import org.assessment.util.CommonUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentMapper assignmentMapper;
    private final CourseServiceClient courseServiceClient;
    private final S3Service s3Service;

 
    @Override
    public AssignmentResponse createAssignment(CreateAssignmentRequest request, MultipartFile file) {

        log.info("Create assignment started. courseId={}, title={}",
                request.getCourseId(), request.getTitle());

        if (request.getCourseId() == null || request.getCourseId().isBlank()) {
            throw new ValidationException("Course ID must not be empty");
        }

        CourseResponse course;

        try {
            Boolean exists = courseServiceClient.courseExists(request.getCourseId());

            log.info("Course existence check completed. courseId={}, exists={}",
                    request.getCourseId(), exists);

            if (exists == null || !exists) {
                throw new ResourceNotFoundException("Course not found with id: " + request.getCourseId());
            }

            course = courseServiceClient.getCourseById(request.getCourseId());

            log.info("Course details fetched. courseId={}, title={}",
                    request.getCourseId(), course.getTitle());

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to validate/fetch course via Feign. courseId={}",
                    request.getCourseId(), e);
            throw new ValidationException("Failed to validate course: " + e.getMessage());
        }

        String userId = CommonUtil.extractUserIdFromRequest();

        Assignment assignment = assignmentMapper.toEntity(request, userId);

        if (course != null && course.getTitle() != null) {
            assignment.setCourseName(course.getTitle());
        }

        if (assignment.getAssignmentId() == null || assignment.getAssignmentId().isBlank()) {
            throw new ValidationException("Assignment ID was not generated");
        }

        if (file != null && !file.isEmpty()) {
            String fileUrl = s3Service.uploadFile(file, "assignments");
            assignment.setAssignmentFileUrl(fileUrl);
        }

        Assignment saved = assignmentRepository.save(assignment);
        return assignmentMapper.toResponse(saved);
    }
    @Override
    public AssignmentResponse getAssignmentById(String assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));
        return assignmentMapper.toResponse(assignment);
    }

    @Override
    public List<AssignmentResponse> getAllAssignments() {
        return assignmentRepository.findAll().stream()
                .map(assignmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentResponse> getAssignmentsByCourse(String courseId) {
        return assignmentRepository.findByCourseId(courseId).stream()
                .map(assignmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentResponse> getAssignmentsByInstructor(String instructorId) {
        return assignmentRepository.findByTrainerId(instructorId).stream()
                .map(assignmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AssignmentResponse updateAssignment(String assignmentId, UpdateAssignmentRequest request, MultipartFile file) {

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        if (request.getTitle() != null) {
            assignment.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            assignment.setDescription(request.getDescription());
        }

        if (request.getAssignmentType() != null) {
            assignment.setAssignmentType(request.getAssignmentType());
        }

        if (request.getDifficultyLevel() != null) {
            assignment.setDifficultyLevel(request.getDifficultyLevel());
        }

        if (request.getStatus() != null) {
            assignment.setStatus(request.getStatus());
        }

        if (request.getTotalMarks() != null) {
            assignment.setTotalMarks(request.getTotalMarks());
        }

        if (request.getPassMarks() != null) {
            assignment.setPassMarks(request.getPassMarks());
        }

        if (request.getDueDate() != null) {
            assignment.setDueDate(request.getDueDate().toString());
        }

        // If courseId is sent in update, validate it and refresh courseName
        if (request.getCourseId() != null && !request.getCourseId().isBlank()) {

            Boolean exists = courseServiceClient.courseExists(request.getCourseId());

            if (exists == null || !exists) {
                throw new ResourceNotFoundException("Course not found with id: " + request.getCourseId());
            }

            CourseResponse course = courseServiceClient.getCourseById(request.getCourseId());

            assignment.setCourseId(request.getCourseId());
            assignment.setCourseName(course.getTitle());
        }

        // If old assignment has courseId but courseName is missing, fill it
        if ((assignment.getCourseName() == null || assignment.getCourseName().isBlank())
                && assignment.getCourseId() != null
                && !assignment.getCourseId().isBlank()) {

            CourseResponse course = courseServiceClient.getCourseById(assignment.getCourseId());
            assignment.setCourseName(course.getTitle());
        }

        if (file != null && !file.isEmpty()) {

            if (assignment.getAssignmentFileUrl() != null) {
                s3Service.deleteFile(assignment.getAssignmentFileUrl());
            }

            String fileUrl = s3Service.uploadFile(file, "assignments");
            assignment.setAssignmentFileUrl(fileUrl);
        }

        assignment.setUpdatedAt(LocalDateTime.now().toString());

        Assignment saved = assignmentRepository.save(assignment);
        return assignmentMapper.toResponse(saved);
    }

    @Override
    public void deleteAssignment(String assignmentId) {
        assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));
        assignmentRepository.deleteById(assignmentId);
    }
    @Override
    public String downloadAssignmentFile(String assignmentId) {

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        if (assignment.getAssignmentFileUrl() == null || assignment.getAssignmentFileUrl().isBlank()) {
            throw new ResourceNotFoundException("Assignment file not found");
        }

        return s3Service.generatePresignedUrl(assignment.getAssignmentFileUrl());
    }
}
