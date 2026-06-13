package org.assessment.service.impl;

import lombok.RequiredArgsConstructor;
import org.assessment.dto.request.SubmitAssignmentRequest;
import org.assessment.dto.response.SubmissionResponse;
import org.assessment.entity.Assignment;
import org.assessment.entity.Submission;
import org.assessment.enums.AssignmentStatus;
import org.assessment.enums.AssignmentType;
import org.assessment.exception.ResourceNotFoundException;
import org.assessment.exception.ValidationException;
import org.assessment.mapper.SubmissionMapper;
import org.assessment.repository.AssignmentRepository;
import org.assessment.repository.SubmissionRepository;
import org.assessment.service.SubmissionService;
import org.assessment.storage.S3Service;
import org.assessment.util.CommonUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionMapper submissionMapper;
    private final S3Service s3Service;

    @Override
    public SubmissionResponse submitAssignment(SubmitAssignmentRequest request, MultipartFile file) {
        String studentId = CommonUtil.extractUserIdFromRequest();
        Assignment assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        if (assignment.getStatusEnum() != AssignmentStatus.PUBLISHED) {
            throw new ValidationException("Assignment is not open for submissions");
        }

        submissionRepository.findByAssignmentIdAndStudentId(request.getAssignmentId(), studentId)
                .ifPresent(s -> { throw new ValidationException("Already submitted for this assignment"); });

        Submission submission = submissionMapper.toEntity(request, studentId);

        if (assignment.getAssignmentTypeEnum() == AssignmentType.FILE_UPLOAD && file != null && !file.isEmpty()) {
            submission.setFileUrl(s3Service.uploadFile(file, "submissions"));
        }

        return submissionMapper.toResponse(submissionRepository.save(submission));
    }

    @Override
    public SubmissionResponse getSubmissionById(String id) {
        return submissionMapper.toResponse(
                submissionRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + id))
        );
    }

    @Override
    public List<SubmissionResponse> getSubmissionsByAssignment(String assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId)
                .stream().map(submissionMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<SubmissionResponse> getSubmissionsByStudent(String studentId) {
        return submissionRepository.findByStudentId(studentId)
                .stream().map(submissionMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public SubmissionResponse getSubmissionByAssignmentAndStudent(String assignmentId, String studentId) {
        return submissionMapper.toResponse(
                submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Submission not found"))
        );
    }
}
