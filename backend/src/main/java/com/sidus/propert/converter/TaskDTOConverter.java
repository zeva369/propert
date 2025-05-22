package com.sidus.propert.converter;


import com.sidus.propert.dto.TaskDTO;
import com.sidus.propert.model.entity.Project;
import com.sidus.propert.model.entity.Task;
import org.springframework.core.convert.converter.Converter;

import java.util.UUID;


public class TaskDTOConverter implements Converter<TaskDTO, Task>{

    @Override
    public Task convert(TaskDTO source) {
        Task task = new Task();
        task.setId(source.id());
		task.setLabel(source.label());
		task.setDescription(source.description());
		task.setLength(source.length());
		Project project = new Project();
		project.setId(source.projectId());
		task.setProject(project);
		if (!source.predecessors().isEmpty()) {
			//Fill the list with fake tasks (they must be hydrated later)
			for  (String id : source.predecessors()) {
				Task pred = new Task();
                pred.setId(UUID.fromString(id));
				task.getPredecessors().add(pred);
			}
		}
        return task;
    }

}
