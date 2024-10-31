package com.seva.propert.service;

import java.util.List;

import com.seva.propert.exception.DuplicatedElementException;
import com.seva.propert.exception.ElementNotFoundException;
import com.seva.propert.model.entity.Task;

public interface TaskService extends EntityService<Task, String>{
    public List<Task> findByDescriptionContaining(String pattern);
    public Task create(Task taskIn) throws DuplicatedElementException;
    public void deleteById(String id) throws ElementNotFoundException;
}
