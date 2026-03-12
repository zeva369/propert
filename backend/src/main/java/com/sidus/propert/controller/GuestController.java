package com.sidus.propert.controller;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sidus.propert.dto.TaskDTO;
import com.sidus.propert.model.pert.TaskElement;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sidus.propert.context.ErrorMessages;
import com.sidus.propert.model.pert.Workflow;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/guest/projects")
@RequiredArgsConstructor
public class GuestController {

    private final ErrorMessages errorMessages;
	private final ConversionService conversionService;

    /**
     * This endpoint processed a list of tasks with its dependencies and returns a Workflow object.
     * The Workflow object contains the nodes and edges of the resulting graph.
     *
     * @param ts (List of tasks)
     * @return ResponseEntity<Workflow>   (Workflow object with nodes and edges)
     */
    @PostMapping
    public ResponseEntity<Workflow> processTaskList(@RequestBody List<TaskDTO> ts) {

        //Convert from List<TaskDTO> to Map<String,TaskElement>
        Map<String, TaskElement> taskElements = ts.stream()
                .map(dto -> conversionService.convert(dto, TaskElement.class))
                .collect(Collectors.toMap(TaskElement::getId,Function.identity()
                         //Map.Entry::getKey,
                        //entry -> TaskElement.fromTask(entry.getValue())
                ));

        Workflow workflow = new Workflow(taskElements);
        workflow.checkAndInitialize();
        return ResponseEntity.ok(workflow);
    }
}
