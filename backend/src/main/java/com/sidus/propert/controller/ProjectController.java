package com.sidus.propert.controller;

import java.util.List;

import com.sidus.propert.dto.TaskDTO;
import com.sidus.propert.dto.TaskInDTO;
import com.sidus.propert.exception.DuplicatedElementException;
import com.sidus.propert.exception.ElementNotFoundException;
import com.sidus.propert.exception.ProperBackendException;
import com.sidus.propert.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
@RequiredArgsConstructor
public class ProjectController {

    private ErrorMessages errorMessages;
    private final ProjectService service;

	/**
	 * Return a list with all Projects
	 * If the user is ADMIN, it can filter by name pattern or return all projects independently of the user.
	 * If the user is USER, it can only return projects that belong to the user.
	 *
	 * @param namePattern (Optional) A pattern to filter projects by name.
	 * @return ResponseEntity with a list of Projects matching the criteria.
	 */
    @GetMapping
    public ResponseEntity<List<Project>> findAll(@RequestParam(value = "name-pattern", required = false) String namePattern,
												 Authentication auth) {

		boolean isAdmin = auth.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
		boolean isUser = auth.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_USER"));

		if (isAdmin) {
			if (namePattern != null && !namePattern.isBlank())
				return ResponseEntity.ok(this.service.findByNameContaining(namePattern));
			else
				return ResponseEntity.ok(this.service.findAll());
		} else if (isUser){
			String userId = auth.getName();
			if (namePattern != null && !namePattern.isBlank())
				return ResponseEntity.ok(this.service.findByUserIdAndNameContaining(userId, namePattern));
			else
				return ResponseEntity.ok(this.service.findByUserId(userId));
		}
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> findById(@PathVariable Long id){
        Project foundProject = this.service.findById(id)
            .orElseThrow(() -> new ProperBackendException(HttpStatus.NOT_FOUND, errorMessages.PROJECT_FIND_BY_ID_NOT_FOUND));

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
