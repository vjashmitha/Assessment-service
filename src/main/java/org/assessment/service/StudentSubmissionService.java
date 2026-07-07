package org.assessment.service;

import org.assessment.dto.response.SubmissionResponse;
import org.springframework.web.multipart.MultipartFile;

public interface StudentSubmissionService {
    SubmissionResponse submitAssignment(String studentId, String assignmentId, MultipartFile file);

    SubmissionResponse resubmitAssignment(String studentId, String assignmentId, MultipartFile file);

    String getAssignmentDownloadUrl(String studentId, String assignmentId);

    SubmissionResponse getMySubmission(String studentId, String assignmentId);

    void deleteSubmission(String studentId, String assignmentId);
	
    String downloadSubmission(String studentId, String assignmentId);

}
