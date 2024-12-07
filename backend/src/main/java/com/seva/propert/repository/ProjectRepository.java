package com.seva.propert.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.seva.propert.model.entity.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>{
    public List<Project> findByNameContaining(String pattern);
    //What i intended here and suposedly is working is to find by the user attribute's id
    public List<Project> findByUserId(String userId);
}
