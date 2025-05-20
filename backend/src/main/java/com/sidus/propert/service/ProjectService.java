package com.sidus.propert.service;

import java.util.List;

import com.sidus.propert.exception.DuplicatedElementException;
import com.sidus.propert.exception.ElementNotFoundException;
import com.sidus.propert.model.entity.Project;

public interface ProjectService extends EntityService<Project, Long>{
    public List<Project> findByNameContaining(String pattern);
    public List<Project> findByUserId(String userId);
    public Project create(Project projectIn) throws DuplicatedElementException;
    public void deleteById(Long id) throws ElementNotFoundException;
}
