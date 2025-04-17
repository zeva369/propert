package com.seva.propert.service;

import java.util.List;

import com.seva.propert.exception.DuplicatedElementException;
import com.seva.propert.exception.ElementNotFoundException;
import com.seva.propert.model.entity.Project;

public interface ProjectService extends EntityService<Project, Long>{
    public List<Project> findByNameContaining(String pattern);
    public List<Project> findByUserId(String userId);
    public Project create(Project projectIn) throws DuplicatedElementException;
    public void deleteById(Long id) throws ElementNotFoundException;
}
