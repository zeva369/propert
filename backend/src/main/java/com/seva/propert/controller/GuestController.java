package com.seva.propert.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.seva.propert.context.ErrorMessages;
import com.seva.propert.dto.TaskInDTO;
import com.seva.propert.exception.ProperBackendException;
import com.seva.propert.exception.WorkFlowLoopException;
import com.seva.propert.model.entity.Task;
import com.seva.propert.model.pert.Workflow;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/guest/projects")
public class GuestController {

    @Autowired
    private ErrorMessages errorMessages;
    
    @Autowired
	private ConversionService conversionService;

    private Task getById(String id, List<Task> tasks) {
        for (Task t : tasks) {
            if (t.getId().equalsIgnoreCase(id)) return t;
        }
        return null;
    }

    @PostMapping
    public ResponseEntity<Workflow> processTaskList(@RequestBody List<TaskInDTO> ts) {
        List<Task> tasks = ts.stream()
                            .map(dto -> conversionService.convert(dto, Task.class))
                            .collect(Collectors.toCollection(ArrayList::new));

        //new ArrayList<>();
        //for (TaskInDTO dto : ts) tasks.add(conversionService.convert(dto, Task.class));

        //Once the task list is converted we need to
        //hidrate each task's predecessors instances
        for (Task t: tasks) {
            if (!t.getPredecessors().isEmpty()){
                List<Task> predecessors = new ArrayList<>();
                for (Task pred : t.getPredecessors()) {
                    Task task = getById(pred.getId(),tasks);
                    Task predTask = new Task();
                    predTask.setId(task.getId());
                    predTask.setDescription(task.getDescription());
                    predTask.setLength(task.getLength());
                    predTask.setDependencies(Task.cloneCollection(task.getDependencies()));
                    predTask.setPredecessors(Task.cloneCollection(task.getPredecessors()));
                    predecessors.add(predTask);
                }
                t.setPredecessors(predecessors);
            }
        }

        Workflow workflow = new Workflow(tasks);
        try {
            workflow.checkAndInitialize();
        } catch (WorkFlowLoopException e) {
            throw new ProperBackendException(HttpStatus.CONFLICT, errorMessages.WORKFLOW_LOOP);
        }
        return ResponseEntity.ok(workflow);
    }
}
