package com.sidus.propert.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.sidus.propert.model.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>{
    List<Task> findByDescriptionContaining(String pattern);
    List<Task> findByProjectId(Long projectId);
    Optional<Task> findByProjectIdAndLabel(Long projectId, String label);
}
