package com.seva.propert.converter;


import org.springframework.core.convert.converter.Converter;

import com.seva.propert.dto.TaskInDTO;
import com.seva.propert.model.entity.Task;


public class TaskInConverter implements Converter<TaskInDTO, Task>{

    @Override
    public Task convert(TaskInDTO source) {
        Task task = new Task();
        task.setId(source.getId());
		task.setDescription(source.getDescription());
		task.setLength(source.getLength());
		if (!source.getPredecessors().isEmpty()) {
			//Fill the list with fake tasks (they must be hidrated later)
			for  (String id : source.getPredecessors()) {
				Task pred = new Task();
                pred.setId(id);
				task.getPredecessors().add(pred);
			}
		}
        return task;
    }

}
