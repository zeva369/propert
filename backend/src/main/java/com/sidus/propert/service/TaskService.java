package com.sidus.propert.service;

import java.util.List;

import com.sidus.propert.exception.DuplicatedElementException;
import com.sidus.propert.exception.ElementNotFoundException;
import com.sidus.propert.model.entity.Task;

public interface TaskService extends EntityService<Task, String>{
    public List<Task> findByDescriptionContaining(String pattern);
    public List<Task> findByProjectId(Long projectId);
    public Task create(Task taskIn) throws DuplicatedElementException;
    public void deleteById(String id) throws ElementNotFoundException;
}
