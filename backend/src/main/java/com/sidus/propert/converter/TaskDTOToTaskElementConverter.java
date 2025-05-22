package com.sidus.propert.converter;

import com.sidus.propert.dto.TaskDTO;
import com.sidus.propert.model.pert.TaskElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;

import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class TaskDTOToTaskElementConverter implements Converter<TaskDTO, TaskElement> {

    @Override
    public TaskElement convert(TaskDTO source) {
        TaskElement task = new TaskElement();
        task.setId(source.id().toString());
        task.setLabel(source.label());
        task.setDescription(source.description());
        task.setLength(source.length());
        String predecessors = String.join(",", source.predecessors());

        task.setPredecessors(predecessors);
        //We don't set the dependencies because they are not in the DTO but
        //they will be calculated inside the Workflow class

        return task;
    }
}
