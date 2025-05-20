package com.sidus.propert.service.impl;

import java.util.List;
import java.util.Optional;

import com.sidus.propert.exception.ElementNotFoundException;
import com.sidus.propert.model.entity.Task;
import org.springframework.stereotype.Service;

import com.sidus.propert.exception.DuplicatedElementException;
import com.sidus.propert.repository.TaskRepository;
import com.sidus.propert.service.TaskService;

@Service
public class TaskServiceImpl implements TaskService{

    private final TaskRepository repo;

    public TaskServiceImpl(TaskRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Task> findAll() {
        return repo.findAll();
    }

    @Override
    public Optional<Task> findById(String id) throws IllegalArgumentException {
        return repo.findById(id);
    }

    @Override
    public List<Task> findByDescriptionContaining(String pattern) {
        return repo.findByDescriptionContaining(pattern);
    }

    
    @Override
    public List<Task> findByProjectId(Long projectId) {
        return repo.findByProjectId(projectId);
    }

    @Override
    public Task save(Task task) {
        return repo.save(task);
    }

    @Override
    public Task create(Task taskIn) throws DuplicatedElementException {
        if (taskIn.getId() != null) {
			Optional<Task> oTask = findById(taskIn.getId());
			if (oTask.isPresent()) 	throw new DuplicatedElementException();
		}
        return this.save(taskIn);
    }

    @Override
    public void delete(Task task) {
        repo.delete(task);
    }

    @Override
    public void deleteById(String id) throws ElementNotFoundException {
        Optional<Task> foundTask= null;
		try {
			foundTask = findById(id);
		} catch (Exception e) {
			throw new ElementNotFoundException();
		}
		if (!foundTask.isPresent())
			throw new ElementNotFoundException();
		delete(foundTask.get());
    }

    

}
