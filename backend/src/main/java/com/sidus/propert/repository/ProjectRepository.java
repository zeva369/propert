package com.sidus.propert.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sidus.propert.model.entity.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>{
    List<Project> findByNameContaining(String pattern);
    List<Project> findByUserIdAndNameContaining(String userId, String pattern);

    List<Project> findByUserId(String userId);
}
