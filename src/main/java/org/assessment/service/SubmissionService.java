package org.assessment.service;

import org.assessment.dto.request.SubmitAssignmentRequest;
import org.assessment.dto.response.SubmissionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SubmissionService {
    SubmissionResponse submitAssignment(SubmitAssignmentRequest request, MultipartFile file);
    SubmissionResponse getSubmissionById(String id);
    List<SubmissionResponse> getSubmissionsByAssignment(String assignmentId);
    List<SubmissionResponse> getSubmissionsByStudent(String studentId);
    SubmissionResponse getSubmissionByAssignmentAndStudent(String assignmentId, String studentId);
}
