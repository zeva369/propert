package com.sidus.propert.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sidus.propert.model.entity.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>{
    public List<Project> findByNameContaining(String pattern);

    //What i intended here, and apparently is working, is to find the project by the user's id attribute
    public List<Project> findByUserId(String userId);
}
