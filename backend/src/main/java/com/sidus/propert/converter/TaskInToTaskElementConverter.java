package com.sidus.propert.converter;

import com.sidus.propert.dto.TaskInDTO;
import com.sidus.propert.model.pert.TaskElement;
import org.springframework.core.convert.converter.Converter;

public class TaskInToTaskElementConverter implements Converter<TaskInDTO, TaskElement> {

    @Override
    public TaskElement convert(TaskInDTO source) {
        TaskElement task = new TaskElement();
        task.setId(source.getId());
        task.setDescription(source.getDescription());
        task.setLength(source.getLength());
        task.setPredecessors(String.join(",", source.getPredecessors()));
        //We don't set the dependencies because they are not in the DTO but
        //they will be calculated inside the Workflow class

        return task;
    }
}
