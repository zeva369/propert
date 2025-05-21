package com.sidus.propert.service;

import java.util.List;

import com.sidus.propert.dto.TaskDTO;
import com.sidus.propert.dto.TaskInDTO;
import com.sidus.propert.exception.DuplicatedElementException;
import com.sidus.propert.exception.ElementNotFoundException;
import com.sidus.propert.model.entity.Project;

public interface ProjectService extends EntityService<Project, Long>{
    List<Project> findByNameContaining(String pattern);
    List<Project> findByUserId(String userId);
    Project create(Project projectIn) throws DuplicatedElementException;
    void deleteById(Long id) throws ElementNotFoundException;
    TaskDTO createTask(Long projectId, TaskInDTO taskIn) throws DuplicatedElementException;
}
