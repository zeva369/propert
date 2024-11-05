package com.seva.propert.controller;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.seva.propert.context.ErrorMessages;
import com.seva.propert.exception.DuplicatedElementException;
import com.seva.propert.exception.ElementNotFoundException;
import com.seva.propert.exception.ProperBackendException;
import com.seva.propert.exception.ValidationException;
import com.seva.propert.model.entity.Task;
import com.seva.propert.service.TaskService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/tasks")
@Validated
public class TaskController {

	private final TaskService service;

	public TaskController(TaskService service) {
		this.service = service;
	}

	@GetMapping
	public ResponseEntity<List<Task>> findAll(
			@RequestParam(value = "description", required = false) String description) {
		if (description == null)
			return ResponseEntity.ok(this.service.findAll());
		else
			return ResponseEntity.ok(this.service.findByDescriptionContaining(description));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Task> findById(@PathVariable String id) {
		Task foundTask = this.service.findById(id)
				.orElseThrow(() -> new ProperBackendException(HttpStatus.NOT_FOUND,
						ErrorMessages.TASK_FIND_BY_ID_NOT_FOUND));
		return ResponseEntity.ok(foundTask);
	}

	// ---- Predecessors ----
	@GetMapping("/{id}/predecessors")
	public ResponseEntity<List<Task>> findPredecessors(@PathVariable String id) {
		Task foundTask = this.service.findById(id)
				.orElseThrow(() -> new ProperBackendException(HttpStatus.NOT_FOUND,
						ErrorMessages.TASK_FIND_BY_ID_NOT_FOUND));

		return ResponseEntity.ok(foundTask.getPredecessors());
	}

	//In the request body only the task-id is needed, that's why i'm not using validation
	@PostMapping("/{id}/predecessors")
	public ResponseEntity<Task> addPredecessor(@PathVariable String id, @RequestBody Task taskIn) {
		//Only check that both tasks exists
		Task foundTask = this.service.findById(id)
				.orElseThrow(() -> new ProperBackendException(HttpStatus.NOT_FOUND,
						ErrorMessages.TASK_FIND_BY_ID_NOT_FOUND));

		Task predTask = this.service.findById(taskIn.getId())
				.orElseThrow(() -> new ProperBackendException(HttpStatus.NOT_FOUND,
						ErrorMessages.TASK_FIND_BY_ID_NOT_FOUND));

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
			throw new ProperBackendException(HttpStatus.CONFLICT, ErrorMessages.TASK_CREATE_DUPLICATED_ELEMENT);
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
						() -> new ProperBackendException(HttpStatus.NOT_FOUND, ErrorMessages.PROJECT_UPDATE_NOT_FOUND));

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
			throw new ProperBackendException(HttpStatus.NOT_FOUND, ErrorMessages.TASK_DELETE_BY_ID_NOT_FOUND);
		} catch (Exception ex) {
			throw new ProperBackendException(HttpStatus.INTERNAL_SERVER_ERROR,
					ErrorMessages.PROJECT_DELETE_CANT_DELETE);
		}
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
	}

}
