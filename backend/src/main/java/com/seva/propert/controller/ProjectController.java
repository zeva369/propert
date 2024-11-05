package com.seva.propert.controller;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seva.propert.context.ErrorMessages;
import com.seva.propert.exception.DuplicatedElementException;
import com.seva.propert.exception.ElementNotFoundException;
import com.seva.propert.exception.ProperBackendException;
import com.seva.propert.exception.ValidationException;
import com.seva.propert.model.entity.Project;
import com.seva.propert.service.ProjectService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service){
        this.service = service;
    }
    
    @GetMapping
    public ResponseEntity<List<Project>> findAll(@RequestParam(value = "name-pattern", required = false) String namePattern){
        if (namePattern == null) return ResponseEntity.ok(this.service.findAll());
		else return ResponseEntity.ok(this.service.findByNameContaining(namePattern));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> findById(@PathVariable Long id){
        Project foundProject = this.service.findById(id)
            .orElseThrow(() -> new ProperBackendException(HttpStatus.NOT_FOUND, ErrorMessages.PROJECT_FIND_BY_ID_NOT_FOUND));
			
			final ObjectMapper mapper = new ObjectMapper();
			try {
				log.info(mapper.writeValueAsString(foundProject.getWorkflow()));
			} catch (Exception e) {
	
			}
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
			throw new ProperBackendException(HttpStatus.CONFLICT, ErrorMessages.PROJECT_CREATE_DUPLICATED_ELEMENT);
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
				.orElseThrow(() -> new ProperBackendException(HttpStatus.NOT_FOUND, ErrorMessages.PROJECT_UPDATE_NOT_FOUND));

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
			throw new ProperBackendException(HttpStatus.NOT_FOUND, ErrorMessages.PROJECT_DELETE_BY_ID_NOT_FOUND);
		} catch (Exception ex) {
			throw new ProperBackendException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessages.PROJECT_DELETE_CANT_DELETE);
		}
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
	}
}
