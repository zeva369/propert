package com.sidus.propert.service.impl;

import java.util.List;
import java.util.Optional;

import com.sidus.propert.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import com.sidus.propert.exception.DuplicatedElementException;
import com.sidus.propert.exception.ElementNotFoundException;
import com.sidus.propert.model.entity.Project;
import com.sidus.propert.service.ProjectService;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository repo;

    public ProjectServiceImpl(ProjectRepository repo){
        this.repo = repo;
    }

    @Override
    public List<Project> findAll() {
        return repo.findAll();
    }

    @Override
    public Optional<Project> findById(Long id) throws IllegalArgumentException {
        return repo.findById(id);
    }
    
    @Override
    public List<Project> findByUserId(String userId) {
        return repo.findByUserId(userId);
    }

    @Override
    public List<Project> findByNameContaining(String pattern) {
        return repo.findByNameContaining(pattern);
    }
    
    @Override
    public Project save(Project project) {
        return repo.save(project);
    }

    @Override
    public void delete(Project project) {
        repo.delete(project);
    }

    @Override
    public Project create(Project projectIn) throws DuplicatedElementException {
        if (projectIn.getId() != null) {
			Optional<Project> oProject = findById(projectIn.getId());
			if (oProject.isPresent())
				throw new DuplicatedElementException();
		}
        return this.save(projectIn);
    }

    @Override
	public void deleteById(Long id) throws ElementNotFoundException {
		Optional<Project> foundProject = null;
		try {
			foundProject = findById(id);
		} catch (Exception e) {
			throw new ElementNotFoundException();
		}
		if (!foundProject.isPresent())
			throw new ElementNotFoundException();
		delete(foundProject.get());

	}



}
