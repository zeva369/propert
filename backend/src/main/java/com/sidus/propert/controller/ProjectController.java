package com.sidus.propert.controller;

import java.util.List;

import com.sidus.propert.dto.TaskDTO;
import com.sidus.propert.dto.TaskInDTO;
import com.sidus.propert.exception.DuplicatedElementException;
import com.sidus.propert.exception.ElementNotFoundException;
import com.sidus.propert.exception.ProperBackendException;
import com.sidus.propert.exception.ValidationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.sidus.propert.context.ErrorMessages;
import com.sidus.propert.model.entity.Project;
import com.sidus.propert.service.ProjectService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/authenticated/projects")
public class ProjectController {

	@Autowired
    private ErrorMessages errorMessages;

    private final ProjectService service;

    public ProjectController(ProjectService service){
        this.service = service;
    }
    
    @GetMapping
    public ResponseEntity<List<Project>> findAll(@RequestParam(value = "name-pattern", required = false) String namePattern,
												 @RequestParam(value = "userId", required = false) String userId){
        if (namePattern == null && userId == null) 
			return ResponseEntity.ok(this.service.findAll());
		else if (namePattern != null)
			return ResponseEntity.ok(this.service.findByNameContaining(namePattern));
		else 
			return ResponseEntity.ok(this.service.findByUserId(userId));										
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> findById(@PathVariable Long id){
        Project foundProject = this.service.findById(id)
            .orElseThrow(() -> new ProperBackendException(HttpStatus.NOT_FOUND, errorMessages.PROJECT_FIND_BY_ID_NOT_FOUND));
			
			// final ObjectMapper mapper = new ObjectMapper();
			// try {
			// 	// log.info(mapper.writeValueAsString(foundProject.getWorkflow()));
			// } catch (Exception e) {
	
			// }
        return ResponseEntity.ok(foundProject);
    }

    @PostMapping
    public ResponseEntity<Project> create(@Valid @RequestBody Project projectIn, BindingResult result) {
        if (result.hasErrors()) {
			throw new ValidationException(result);
		}

        Project createdProject = null;
		try {
			createdProject = this.service.create(projectIn);
		} catch (DuplicatedElementException e) {
			throw new ProperBackendException(HttpStatus.CONFLICT, errorMessages.PROJECT_CREATE_DUPLICATED_ELEMENT);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
    }

    // Full update (All fields supplied in the body)
	@PutMapping("/{id}")
	public ResponseEntity<Project> update(@PathVariable Long id, @Valid @RequestBody Project projectDetails, BindingResult result) {
		if (result.hasErrors()) {
			throw new ValidationException(result);
		}
		
		Project foundProject = this.service.findById(id)
				.orElseThrow(() -> new ProperBackendException(HttpStatus.NOT_FOUND, errorMessages.PROJECT_UPDATE_NOT_FOUND));

		BeanUtils.copyProperties(projectDetails, foundProject);
		Project savedProject = this.service.save(foundProject);
		
		return ResponseEntity.status(HttpStatus.OK).body(savedProject);
	}
    
    // Delete an existing Project
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteById(@PathVariable(value = "id") Long id) {
		try {
			this.service.deleteById(id);
		} catch (ElementNotFoundException ex) {
			throw new ProperBackendException(HttpStatus.NOT_FOUND, errorMessages.PROJECT_DELETE_BY_ID_NOT_FOUND);
		} catch (Exception ex) {
			throw new ProperBackendException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessages.PROJECT_DELETE_CANT_DELETE);
		}
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
	}

	//Tasks
	@PostMapping("/{id}/tasks")
	public ResponseEntity<TaskDTO> createTask(@PathVariable(value = "id") Long projectId,
										 	  @Valid @RequestBody TaskInDTO taskIn) {
		TaskDTO createdTask = this.service.createTask(projectId, taskIn);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
	}
}
