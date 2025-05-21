package com.sidus.propert.service.impl;

import java.util.List;
import java.util.Optional;

import com.sidus.propert.context.ErrorMessages;
import com.sidus.propert.dto.TaskDTO;
import com.sidus.propert.exception.ElementNotFoundException;
import com.sidus.propert.model.entity.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import com.sidus.propert.repository.TaskRepository;
import com.sidus.propert.service.TaskService;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService{

    private final ErrorMessages errorMessages;
    private final TaskRepository repo;
    private final ConversionService conversionService;

    @Override
    public List<TaskDTO> findAll() {
        return repo.findAll().stream()
                .map(task -> conversionService.convert(task, TaskDTO.class))
                .toList();
    }

    @Override
    public Optional<TaskDTO> findById(String id) throws IllegalArgumentException {
        return repo.findById(id)
                .map(task -> conversionService.convert(task, TaskDTO.class));
    }

    @Override
    public List<TaskDTO> findByDescriptionContaining(String pattern) {
        return repo.findByDescriptionContaining(pattern).stream()
                .map(task -> conversionService.convert(task, TaskDTO.class))
                .toList();
    }

    @Override
    public List<TaskDTO> findByProjectId(Long projectId) {
        return repo.findByProjectId(projectId).stream()
                .map(task -> conversionService.convert(task, TaskDTO.class))
                .toList();
    }

    @Override
    public TaskDTO save(TaskDTO task) {
        if (task == null) throw new IllegalArgumentException("Task is null");

        Task taskToSave = this.conversionService.convert(task, Task.class);
        if (taskToSave == null) throw new IllegalArgumentException("Error processing the task (unable to convert).");

        Task taskSaved = this.repo.save(taskToSave);
        return this.conversionService.convert(taskSaved, TaskDTO.class);
    }

//    @Override
//    public TaskDTO create(TaskDTO taskIn) throws DuplicatedElementException {
//        if (taskIn.id() != null) {
//			Optional<Task> oTask = findById(taskIn.id());
//			if (oTask.isPresent()) throw new DuplicatedElementException(errorMessages.TASK_CREATE_DUPLICATED_ELEMENT);
//		}
//        Task taskToSave = this.conversionService.convert(taskIn, Task.class);
//        Task taskSaved = this.save(taskToSave);
//        return this.conversionService.convert(taskSaved, TaskDTO.class);
//    }

    @Override
    public void delete(TaskDTO task) {
        if (task == null) throw new IllegalArgumentException("Task is null");
        if (task.id() == null) throw new IllegalArgumentException("Task id is null");
        Optional<TaskDTO> foundTask = this.findById(task.id());
        if (foundTask.isEmpty()) throw new ElementNotFoundException(errorMessages.TASK_DELETE_NOT_FOUND);

        Task taskToDelete = this.conversionService.convert(task, Task.class);
        repo.delete(taskToDelete);
    }

    @Override
    public void deleteById(String id) throws ElementNotFoundException {
        Optional<TaskDTO> foundTask= null;
		try {
			foundTask = findById(id);
		} catch (Exception e) {
			throw new ElementNotFoundException();
		}
		if (!foundTask.isPresent())
			throw new ElementNotFoundException();
		this.delete(foundTask.get());
    }

    

}
