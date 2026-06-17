package org.assessment.service;

import org.assessment.dto.request.CreateAssignmentRequest;
import org.assessment.dto.request.UpdateAssignmentRequest;
import org.assessment.dto.response.AssignmentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AssignmentService {

    AssignmentResponse createAssignment(CreateAssignmentRequest request, MultipartFile file);

    AssignmentResponse getAssignmentById(String assignmentId);

    List<AssignmentResponse> getAllAssignments();

    List<AssignmentResponse> getAssignmentsByCourse(String courseId);

    List<AssignmentResponse> getAssignmentsByInstructor(String instructorId);

    AssignmentResponse updateAssignment(String assignmentId, UpdateAssignmentRequest request, MultipartFile file);

    void deleteAssignment(String assignmentId);
}
