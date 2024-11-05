package com.seva.propert.model.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.seva.propert.validation.annotation.ExistProject;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class Task implements Clonable<Task>{
    @Id
    private String id;
    private String description;
    private Double length;

    //@JsonBackReference
    @ManyToMany
    @JoinTable(
        name = "task_predecessors",
        joinColumns = @JoinColumn(name = "id"),
        inverseJoinColumns = @JoinColumn(name = "predecessor_id")
    )
    private List<Task> predecessors = new ArrayList<>();

    @NotNull
	@ManyToOne
	@JoinColumn(name = "project_id")
    @JsonBackReference
    @ExistProject
    private Project project = null;

    @Column(name="is_dummy")
    private Boolean isDummy = false;

    //PERT's Logic specific attributes
    @Transient
    @JsonIgnore
    private Double earlyStart = -1d;
    @Transient
    @JsonIgnore
    private Double earlyFinish = -1d;
    @Transient
    @JsonIgnore
    private Double lateStart = -1d;
    @Transient
    @JsonIgnore
    private Double lateFinish = -1d;
    
    @Transient
    @JsonIgnore
    private List<Task> dependencies = new ArrayList<>();

    @Override
    public Task clone() {
        Task newTask = new Task();
        newTask.id = this.id;
        newTask.description = this.description;
        newTask.length = this.length;
        newTask.project = this.project;
        newTask.predecessors =  this.predecessors.stream()
                                  .map(Task::clone)
                                  .collect(Collectors.toList());
        newTask.dependencies = this.dependencies.stream()
                                  .map(Task::clone)
                                  .collect(Collectors.toList());
        newTask.isDummy = this.isDummy;
        return newTask;
    }

    
}
