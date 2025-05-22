package com.sidus.propert.model.pert;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import com.sidus.propert.model.entity.Task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TaskElement {
    protected String id;
    protected String label;
    protected String description;
    protected Double length;

    protected String predecessors = "";

    protected Boolean dummy = false;

    protected Double earlyStart = -1d;
    protected Double earlyFinish = -1d;
    protected Double lateStart = -1d;
    protected Double lateFinish = -1d;

    protected String dependencies = "";

    public void addDependency(String dep) {
        this.dependencies += (dependencies.isEmpty() ? "" : ",").concat(dep);
    }

    // Changed these two method's behaviour because replacing strings leads to
    // errors
    // If i replace task D for X and we have a task D2 wi will end in X2
    // Now i search for entire elements
    public void replacePredecessor(String oldTaskId, String newTaskId) {
        String[] elements = this.predecessors.split(",");

        this.predecessors = Arrays.stream(elements)
                .map(element -> element.equals(oldTaskId) ? newTaskId : element) // Reemplazar solo si coincide
                .collect(Collectors.joining(","));
    }

    public void replaceDependency(String oldTaskId, String newTaskId) {
        String[] elements = this.dependencies.split(",");

        this.dependencies = Arrays.stream(elements)
                .map(element -> element.equals(oldTaskId) ? newTaskId : element) // Reemplazar solo si coincide
                .collect(Collectors.joining(","));
    }

    public static TaskElement fromTask(Task t) {
        TaskElement element = new TaskElement();
        element.id = t.getId().toString();
        element.description = t.getDescription();
        element.length = t.getLength();

        element.predecessors = t.getPredecessors().stream()
                .map(Task::getId)
                .map(UUID::toString)
                .collect(Collectors.joining(","));
        element.dependencies = t.getDependencies().stream()
                .map(Task::getId)
                .map(UUID::toString)
                .collect(Collectors.joining(","));

        return element;
    }
}