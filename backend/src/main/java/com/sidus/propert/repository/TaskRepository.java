package com.sidus.propert.repository;

import java.util.List;

import com.sidus.propert.model.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, String>{
    public List<Task> findByDescriptionContaining(String pattern);
    public List<Task> findByProjectId(Long projectId);
}
