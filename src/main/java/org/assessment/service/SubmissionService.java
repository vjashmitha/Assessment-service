package org.assessment.service;

import java.util.List;

import org.assessment.dto.request.SubmitAssignmentRequest;
import org.assessment.dto.response.SubmissionResponse;
import org.assessment.enums.SubmissionStatus;
import org.springframework.web.multipart.MultipartFile;

public interface SubmissionService {
    SubmissionResponse submitAssignment(SubmitAssignmentRequest request, MultipartFile file);
    SubmissionResponse getSubmissionById(String id);
    List<SubmissionResponse> getSubmissionsByAssignment(String assignmentId);
    List<SubmissionResponse> getSubmissionsByStudent(String studentId);
    SubmissionResponse getSubmissionByAssignmentAndStudent(String assignmentId, String studentId);
    String downloadSubmissionFile(String submissionId);
    
    List<SubmissionResponse> getPendingSubmissionsByTrainer(String trainerId);

    List<SubmissionResponse> getSubmissionsByTrainerAndStatus(
            String trainerId,
            SubmissionStatus status
    );
    List<SubmissionResponse> getAllSubmissionsByTrainer(String trainerId);
}
