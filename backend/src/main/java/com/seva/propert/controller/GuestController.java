package com.seva.propert.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private ErrorMessages errorMessages;
	private ConversionService conversionService;

    public GuestController(ErrorMessages errorMessages, ConversionService conversionService) {
        this.errorMessages = errorMessages;
        this.conversionService = conversionService;
    }

    @PostMapping
    public ResponseEntity<Workflow> processTaskList(@RequestBody List<TaskInDTO> ts) {
        
        Map<String, Task> tasks = ts.stream()
                            .map(dto -> conversionService.convert(dto, Task.class))
                            .collect(Collectors.toMap(Task::getId, Function.identity()));   //Function.identity() funciona como si fuese t -> t pero es mas standard

        //Once the task list is converted we need to
        //hidrate each task's predecessors instances
        tasks.forEach((k, t) -> {
            List<Task> predecessors = new ArrayList<>();

            t.getPredecessors().stream()
                .map(p -> tasks.get(p.getId()))
                .forEach(p -> {
                    Task predTask = new Task();
                    predTask.setId(p.getId());
                    predTask.setDescription(p.getDescription());
                    predTask.setLength(p.getLength());
                    predTask.setDependencies(Task.cloneCollection(p.getDependencies()));
                    predTask.setPredecessors(Task.cloneCollection(p.getPredecessors()));
                    predecessors.add(predTask);
                });
            t.setPredecessors(predecessors);
        });

        List<Task> taskList = tasks.values()
            .stream()
            .collect(Collectors.toCollection(ArrayList::new));

        Workflow workflow = new Workflow(taskList);
        try {
            workflow.checkAndInitialize();
        } catch (WorkFlowLoopException e) {
            throw new ProperBackendException(HttpStatus.CONFLICT, errorMessages.WORKFLOW_LOOP);
        }
        return ResponseEntity.ok(workflow);
    }
}
