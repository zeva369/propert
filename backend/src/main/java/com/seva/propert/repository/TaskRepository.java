package com.seva.propert.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.seva.propert.model.entity.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, String>{
    public List<Task> findByDescriptionContaining(String pattern);
}
