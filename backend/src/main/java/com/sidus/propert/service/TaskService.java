package com.sidus.propert.service;

import java.util.List;

import com.sidus.propert.dto.TaskDTO;
import com.sidus.propert.exception.DuplicatedElementException;
import com.sidus.propert.exception.ElementNotFoundException;
import com.sidus.propert.model.entity.Task;

public interface TaskService extends EntityService<TaskDTO, String>{
    public List<TaskDTO> findByDescriptionContaining(String pattern);
    public List<TaskDTO> findByProjectId(Long projectId);
//    public TaskDTO create(TaskDTO taskIn) throws DuplicatedElementException;
    public void deleteById(String id) throws ElementNotFoundException;
}
