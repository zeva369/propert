package com.sidus.propert.controller;

import java.util.List;

import com.sidus.propert.model.entity.Task;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.sidus.propert.context.ErrorMessages;
import com.sidus.propert.exception.DuplicatedElementException;
import com.sidus.propert.exception.ElementNotFoundException;
import com.sidus.propert.exception.ProperBackendException;
import com.sidus.propert.exception.ValidationException;
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
	private final TaskService service;

	@GetMapping
	public ResponseEntity<List<Task>> findAll(
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "projectId", required = false) Long projectId) {
		if (description == null && projectId == null)
			return ResponseEntity.ok(this.service.findAll());
		else if (projectId != null) {
			return ResponseEntity.ok(this.service.findByProjectId(projectId));
		} else 
			return ResponseEntity.ok(this.service.findByDescriptionContaining(description));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Task> findById(@PathVariable String id) {
		Task foundTask = this.service.findById(id)
				.orElseThrow(() -> new ProperBackendException(HttpStatus.NOT_FOUND,
						errorMessages.TASK_FIND_BY_ID_NOT_FOUND));
		return ResponseEntity.ok(foundTask);
	}

	// ---- Predecessors ----
	@GetMapping("/{id}/predecessors")
	public ResponseEntity<List<Task>> findPredecessors(@PathVariable String id) {
		Task foundTask = this.service.findById(id)
				.orElseThrow(() -> new ProperBackendException(HttpStatus.NOT_FOUND,
						errorMessages.TASK_FIND_BY_ID_NOT_FOUND));

		return ResponseEntity.ok(foundTask.getPredecessors());
	}

	//In the request body only the task-id is needed, that's why i'm not using validation
	@PostMapping("/{id}/predecessors")
	public ResponseEntity<Task> addPredecessor(@PathVariable String id, @RequestBody Task taskIn) {
		//Only check that both tasks exists
		Task foundTask = this.service.findById(id)
				.orElseThrow(() -> new ProperBackendException(HttpStatus.NOT_FOUND,
						errorMessages.TASK_FIND_BY_ID_NOT_FOUND));

		Task predTask = this.service.findById(taskIn.getId())
				.orElseThrow(() -> new ProperBackendException(HttpStatus.NOT_FOUND,
						errorMessages.TASK_FIND_BY_ID_NOT_FOUND));

		foundTask.getPredecessors().add(predTask);
		this.service.save(foundTask);
		return ResponseEntity.status(HttpStatus.OK).body(predTask);
	}
	// ----------------------

	@PostMapping
	public ResponseEntity<Task> create(@Valid @RequestBody Task taskIn, BindingResult result) {
		if (result.hasErrors()) {
			throw new ValidationException(result);
		}

		Task createdTask = null;
		try {
			createdTask = this.service.create(taskIn);
		} catch (DuplicatedElementException e) {
			throw new ProperBackendException(HttpStatus.CONFLICT, errorMessages.TASK_CREATE_DUPLICATED_ELEMENT);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
	}

	// Full update (All fields supplied in the body)
	@PutMapping("/{id}")
	public ResponseEntity<Task> update(@PathVariable String id, @Valid @RequestBody Task taskDetails,
			BindingResult result) {
		if (result.hasErrors()) {
			throw new ValidationException(result);
		}

		Task foundTask = this.service.findById(id)
				.orElseThrow(
						() -> new ProperBackendException(HttpStatus.NOT_FOUND, errorMessages.PROJECT_UPDATE_NOT_FOUND));

		BeanUtils.copyProperties(taskDetails, foundTask);
		Task savedTask = this.service.save(foundTask);

		return ResponseEntity.status(HttpStatus.OK).body(savedTask);
	}

	// Delete an existing Task
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteById(@PathVariable(value = "id") String id) {
		try {
			this.service.deleteById(id);
		} catch (ElementNotFoundException ex) {
			throw new ProperBackendException(HttpStatus.NOT_FOUND, errorMessages.TASK_DELETE_BY_ID_NOT_FOUND);
		} catch (Exception ex) {
			throw new ProperBackendException(HttpStatus.INTERNAL_SERVER_ERROR,
					errorMessages.PROJECT_DELETE_CANT_DELETE);
		}
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
	}

}
