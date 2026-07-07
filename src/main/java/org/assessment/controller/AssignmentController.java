package org.assessment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.assessment.dto.request.CreateAssignmentRequest;
import org.assessment.dto.request.UpdateAssignmentRequest;
import org.assessment.dto.response.AssignmentResponse;
import org.assessment.service.AssignmentService;
import org.assessment.util.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(Constants.ASSIGNMENT_BASE_URL)
@RequiredArgsConstructor
public class AssignmentController {

	private final AssignmentService assignmentService;
	private final ObjectMapper objectMapper;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<AssignmentResponse> createAssignment(@RequestPart("request") String requestJson,
			@RequestPart(value = "file", required = false) MultipartFile file) {

		

		CreateAssignmentRequest request;

		try {
			request = objectMapper.readValue(requestJson, CreateAssignmentRequest.class);
		} catch (Exception e) {
			throw new RuntimeException("Invalid assignment request JSON: " + e.getMessage(), e);
		}

		return ResponseEntity.status(HttpStatus.CREATED).body(assignmentService.createAssignment(request, file));
	}

	@GetMapping("/{id}")
	public ResponseEntity<AssignmentResponse> getAssignmentById(@PathVariable String id) {
		return ResponseEntity.ok(assignmentService.getAssignmentById(id));
	}

	@GetMapping
	public ResponseEntity<List<AssignmentResponse>> getAllAssignments() {
		return ResponseEntity.ok(assignmentService.getAllAssignments());
	}

	@GetMapping("/courses/{courseId}")
	public ResponseEntity<List<AssignmentResponse>> getAssignmentsByCourse(@PathVariable String courseId) {
		return ResponseEntity.ok(assignmentService.getAssignmentsByCourse(courseId));
	}

	@GetMapping("/trainers/{trainerId}")
	public ResponseEntity<List<AssignmentResponse>> getAssignmentsByTrainer(@PathVariable String trainerId) {
		return ResponseEntity.ok(assignmentService.getAssignmentsByInstructor(trainerId));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<AssignmentResponse> updateAssignment(@PathVariable String id,
			@RequestPart("request") String requestJson,
			@RequestPart(value = "file", required = false) MultipartFile file) {

		try {
			UpdateAssignmentRequest request = objectMapper.readValue(requestJson, UpdateAssignmentRequest.class);

			return ResponseEntity.ok(assignmentService.updateAssignment(id, request, file));

		} catch (Exception e) {
			throw new RuntimeException("Invalid assignment update JSON", e);
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteAssignment(@PathVariable String id) {
		assignmentService.deleteAssignment(id);
		return ResponseEntity.noContent().build();
	}
	
	@GetMapping("/{id}/download")
	public ResponseEntity<String> downloadAssignmentFile(@PathVariable String id) {
	    return ResponseEntity.ok(assignmentService.downloadAssignmentFile(id));
	}
}