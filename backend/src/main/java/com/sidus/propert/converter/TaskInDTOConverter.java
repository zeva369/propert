package com.sidus.propert.converter;

import com.sidus.propert.dto.TaskDTO;
import com.sidus.propert.dto.TaskInDTO;
import org.springframework.core.convert.converter.Converter;

import java.util.ArrayList;
import java.util.UUID;

public class TaskInDTOConverter implements Converter<TaskInDTO, TaskDTO> {

    @Override
    public TaskDTO convert(TaskInDTO source) {
        return new TaskDTO(
            UUID.randomUUID(),
            source.label(),
            source.description(),
            source.length(),
            null,
            new ArrayList<>(source.predecessors()));
    }
}
