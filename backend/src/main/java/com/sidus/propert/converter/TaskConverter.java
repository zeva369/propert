package com.sidus.propert.converter;

import com.sidus.propert.dto.TaskDTO;
import com.sidus.propert.model.entity.Task;
import org.springframework.core.convert.converter.Converter;

import java.util.ArrayList;

public class TaskConverter implements Converter<Task, TaskDTO> {
    @Override
    public TaskDTO convert(Task source) {
        TaskDTO taskDTO = new TaskDTO(
            source.getId(),
            source.getDescription(),
            source.getLength(),
            source.getProject().getId(),
            new ArrayList<>());

        if (!source.getPredecessors().isEmpty()) {
            // Fill the list with fake tasks (they must be hydrated later)
            for (Task pred : source.getPredecessors()) {
                taskDTO.predecessors().add(pred.getId());
            }
        }
        return taskDTO;
    }
}
