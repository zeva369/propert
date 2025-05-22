package com.sidus.propert.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.sidus.propert.dto.TaskDTO;
import com.sidus.propert.exception.DuplicatedElementException;
import com.sidus.propert.exception.ElementNotFoundException;
import com.sidus.propert.model.entity.Task;

public interface TaskService extends EntityService<TaskDTO, UUID>{
    List<TaskDTO> findByDescriptionContaining(String pattern);
    List<TaskDTO> findByProjectId(Long projectId);
    Optional<TaskDTO> findByProjectIdAndLabel(Long projectId, String label);
//    public TaskDTO create(TaskDTO taskIn) throws DuplicatedElementException;
    void deleteById(UUID id) throws ElementNotFoundException;
}
