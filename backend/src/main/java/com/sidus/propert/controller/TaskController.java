package com.sidus.propert.controller;

import java.util.List;
import java.util.UUID;

import com.sidus.propert.dto.TaskDTO;
import com.sidus.propert.exception.*;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.sidus.propert.context.ErrorMessages;
import com.sidus.propert.service.TaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/authenticated/tasks")
@Validated  // Activa validación en parámetros como @PathVariable, @RequestParam, etc.
@RequiredArgsConstructor
public class TaskController {

    private final ErrorMessages errorMessages;
	private final TaskService taskService;

	@GetMapping
	public ResponseEntity<List<TaskDTO>> findAll(
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "projectId", required = false) Long projectId) {
		if (description == null && projectId == null)
			return ResponseEntity.ok(this.taskService.findAll());
		else if (projectId != null) {
			return ResponseEntity.ok(this.taskService.findByProjectId(projectId));
		} else 
			return ResponseEntity.ok(this.taskService.findByDescriptionContaining(description));
	}

	@GetMapping("/{id}")
	public ResponseEntity<TaskDTO> findById(@PathVariable String id) {
		testUUID(id);
		TaskDTO foundTask = this.taskService.findById(UUID.fromString(id))
				.orElseThrow(() -> new ProperBackendException(HttpStatus.NOT_FOUND,
						errorMessages.TASK_FIND_BY_ID_NOT_FOUND));
		return ResponseEntity.ok(foundTask);
	}

	// ---- Predecessors ----
	@GetMapping("/{id}/predecessors")
	public ResponseEntity<List<TaskDTO>> findPredecessors(@PathVariable String id) {
		testUUID(id);
		TaskDTO foundTask = this.taskService.findById(UUID.fromString(id))
				.orElseThrow(() -> new ProperBackendException(HttpStatus.NOT_FOUND,
						errorMessages.TASK_FIND_BY_ID_NOT_FOUND));

		return ResponseEntity.ok(foundTask.predecessors().stream()
				.map(t -> this.taskService.findById(UUID.fromString(t))
						.orElseThrow(() -> new ProperBackendException(HttpStatus.NOT_FOUND,
								errorMessages.TASK_FIND_BY_ID_NOT_FOUND)))
				.toList());
	}

	@PostMapping("/{id}/predecessors")
	public ResponseEntity<TaskDTO> addPredecessor(@PathVariable String id, @RequestParam(value = "pred-id", required = false) String predId) {
		testUUID(id);
		testUUID(predId);
		//Only check that both tasks exists
		TaskDTO foundTask = this.taskService.findById(UUID.fromString(id))
				.orElseThrow(() -> new ProperBackendException(HttpStatus.NOT_FOUND,
						errorMessages.TASK_FIND_BY_ID_NOT_FOUND));

		TaskDTO predTask = this.taskService.findById(UUID.fromString(predId))
				.orElseThrow(() -> new ProperBackendException(HttpStatus.NOT_FOUND,
						errorMessages.TASK_FIND_BY_ID_NOT_FOUND));

		if (!foundTask.predecessors().contains(predTask.id().toString())) {
			foundTask.predecessors().add(predTask.id().toString());
		}
		this.taskService.save(foundTask);
		return ResponseEntity.status(HttpStatus.OK).body(predTask);
	}
	// ----------------------

	@DeleteMapping("/{id}/predecessors/{pred-id}")
	public ResponseEntity<TaskDTO> removePredecessor(@PathVariable String id, @PathVariable(name="pred-id") String predId) {
		testUUID(id);
		testUUID(predId);

		//Only check that both tasks exists
		TaskDTO foundTask = this.taskService.findById(UUID.fromString(id))
				.orElseThrow(() -> new ProperBackendException(HttpStatus.NOT_FOUND,
						errorMessages.TASK_FIND_BY_ID_NOT_FOUND));

		this.taskService.findById(UUID.fromString(predId))
				.orElseThrow(() -> new ProperBackendException(HttpStatus.NOT_FOUND,
						errorMessages.TASK_FIND_BY_ID_NOT_FOUND));

		if (foundTask.predecessors().contains(predId)) {
			foundTask.predecessors().remove(predId);
		} else {
			throw new TaskPredecessorNotFoundException(errorMessages.TASK_PREDECESSOR_NOT_FOUND);
		}
		this.taskService.save(foundTask);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
	}

	// Full update (All fields supplied in the body)
	@PutMapping("/{id}")
	public ResponseEntity<TaskDTO> update(@PathVariable String id, @Valid @RequestBody TaskDTO taskDetails) {
		testUUID(id);
		TaskDTO foundTask = this.taskService.findById(UUID.fromString(id))
				.orElseThrow(
						() -> new ProperBackendException(HttpStatus.NOT_FOUND, errorMessages.PROJECT_UPDATE_NOT_FOUND));

		BeanUtils.copyProperties(taskDetails, foundTask);
		TaskDTO savedTask = this.taskService.save(foundTask);

		return ResponseEntity.status(HttpStatus.OK).body(savedTask);
	}

	// Delete an existing Task
	@DeleteMapping("/{id}")
	public ResponseEntity<Object> deleteById(@PathVariable(value = "id") String id) {
		testUUID(id);
		try {
			this.taskService.deleteById(UUID.fromString(id));
		} catch (ElementNotFoundException ex) {
			throw new ProperBackendException(HttpStatus.NOT_FOUND, errorMessages.TASK_DELETE_BY_ID_NOT_FOUND);
		} catch (Exception ex) {
			throw new ProperBackendException(HttpStatus.INTERNAL_SERVER_ERROR,
					errorMessages.PROJECT_DELETE_CANT_DELETE);
		}
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
	}

	private void testUUID(String id) {
		try {
			UUID.fromString(id);
		} catch (IllegalArgumentException ex) {
			throw new InvalidTaskIdFormatException();
		}
	}
}
