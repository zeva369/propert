package com.sidus.propert.model.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sidus.propert.validation.annotation.ExistProject;

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
public class Task implements Clonable<Task> {  //, TaskEventListener{
    @Id
    private String id;
    private String description;
    private Double length;

    // @Transient
    // @JsonIgnore
    // private List<Task> depListeners = new ArrayList<>();

    // @Transient
    // @JsonIgnore
    // private List<Task> predListeners = new ArrayList<>();

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
                                  .collect(Collectors.toCollection(ArrayList::new));
        newTask.dependencies = this.dependencies.stream()
                                  .map(Task::clone)
                                  .collect(Collectors.toCollection(ArrayList::new));
        newTask.isDummy = this.isDummy;
        return newTask;
    }

    public static List<Task> cloneCollection(List<Task> source) {
        return source.stream().map(Task::clone).collect(Collectors.toCollection(ArrayList::new));
    }

    // public void replacePredecessor(Task oldTask, Task newTask) {
    //     // Iterator<Task> predecessorsIterator = getPredecessors().iterator();
    //     // while (predecessorsIterator.hasNext()) {
    //     //     Task predecessor = predecessorsIterator.next();
    //     //     if (predecessor.getId().equalsIgnoreCase(oldTask.getId())){
    //     //         predecessorsIterator.remove();
    //     //         break;
    //     //     }
    //     // } 
    //     this.predecessors = this.predecessors.stream()
    //                         .filter(t -> t != oldTask)
    //                         .collect(Collectors.toList());
    //                         //.remove(oldTask);                   
    //     this.predecessors.add(newTask);
    // }

    // private void sendEvent(Task suscriber, TASK_EVENT event) {
    //     switch(event){
    //         case DEPENDENCY_REMOVED -> {
    //             suscriber.onDependencyRemoved(this);
    //         }
    //         case PREDECESSOR_REMOVED -> {
    //             suscriber.onPredecessorRemoved(this);
    //         }
    //     }
    // }

    // private void broadcastEvent(List<Task> listeners, TASK_EVENT event) {
    //     for(Task listener : listeners) sendEvent(listener, event);
    // }

    // public void addDependency(Task dep){
    //     this.dependencies.add(dep);
    //     //Ahora la tarea dependiente recibirá mensajes de esta tarea cuando 
    //     //ocurra algún evento importante
    //     this.depListeners.add(dep);
    // }

    // public void removeDependency(Task dep){
    //     this.dependencies.remove(dep);
    //     //Ahora la tarea dependiente recibirá mensajes de esta tarea cuando 
    //     //ocurra algún evento importante
    //     sendEvent(dep,TASK_EVENT.DEPENDENCY_REMOVED);
    //     //Luego elimino el listener
    //     this.depListeners.remove(dep);
    // }

    // public void addPredecessor(Task pred){
    //     this.predecessors.add(pred);
    //     //Ahora la tarea dependiente recibirá mensajes de esta tarea cuando 
    //     //ocurra algún evento importante
    //     this.predListeners.add(pred);
    // }

    // public void removePredecessor(Task pred){
    //     this.predecessors.remove(pred);
    //     //Ahora la tarea dependiente recibirá mensajes de esta tarea cuando 
    //     //ocurra algún evento importante
    //     sendEvent(pred,TASK_EVENT.PREDECESSOR_REMOVED);
    //     //Luego elimino el listener
    //     this.predListeners.remove(pred);
    // }

    // @Override
    // public void onDependencyRemoved(Task dependency) {
    //     //In this case i don't use removePredecessor to avoid generating a loop
    //     this.predecessors.remove(dependency); 
    //     this.predListeners.remove(dependency);    
    // }

    // @Override
    // public void onPredecessorRemoved(Task predecessor) {
    //     //In this case i don't use removeDependency to avoid generating a loop
    //     this.dependencies.remove(predecessor); 
    //     this.depListeners.remove(predecessor);        
    // }

}
