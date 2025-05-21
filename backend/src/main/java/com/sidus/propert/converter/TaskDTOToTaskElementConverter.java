package com.sidus.propert.converter;

import com.sidus.propert.dto.TaskDTO;
import com.sidus.propert.model.pert.TaskElement;
import org.springframework.core.convert.converter.Converter;

public class TaskDTOToTaskElementConverter implements Converter<TaskDTO, TaskElement> {

    @Override
    public TaskElement convert(TaskDTO source) {
        TaskElement task = new TaskElement();
        task.setId(source.id());
        task.setDescription(source.description());
        task.setLength(source.length());
        task.setPredecessors(String.join(",", source.predecessors()));
        //We don't set the dependencies because they are not in the DTO but
        //they will be calculated inside the Workflow class

        return task;
    }
}
