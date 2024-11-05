package com.seva.propert.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.seva.propert.exception.DuplicatedElementException;
import com.seva.propert.exception.ElementNotFoundException;
import com.seva.propert.model.entity.Task;
import com.seva.propert.repository.TaskRepository;
import com.seva.propert.service.TaskService;

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
