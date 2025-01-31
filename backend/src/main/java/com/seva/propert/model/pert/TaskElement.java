package com.seva.propert.model.pert;

import java.util.Arrays;
import java.util.stream.Collectors;

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

}