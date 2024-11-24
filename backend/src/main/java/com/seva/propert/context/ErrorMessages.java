package com.seva.propert.context;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:messages.properties")
public class ErrorMessages {
    //Project messages
    @Value("${controller.project.find-by-id.not-found}")
    public String PROJECT_FIND_BY_ID_NOT_FOUND;

    @Value("${controller.project.create.duplicated-element}")
    public String PROJECT_CREATE_DUPLICATED_ELEMENT;

    @Value("${controller.project.update.not-found}")
    public String PROJECT_UPDATE_NOT_FOUND;

    @Value("${controller.project.delete-by-id.not-found}")
    public String PROJECT_DELETE_BY_ID_NOT_FOUND;

    @Value("${controller.project.delete-by-id.cant-delete}")
    public String PROJECT_DELETE_CANT_DELETE;

    //Task messages
    @Value("${controller.task.find-by-id.not-found}")
    public String TASK_FIND_BY_ID_NOT_FOUND;

    @Value("${controller.task.create.duplicated-element}")
    public String TASK_CREATE_DUPLICATED_ELEMENT;

    @Value("${controller.task.update.not-found}")
    public String TASK_UPDATE_NOT_FOUND;

    @Value("${controller.task.delete-by-id.not-found}")
    public String TASK_DELETE_BY_ID_NOT_FOUND;

    @Value("${controller.task.delete-by-id.cant-delete}")
    public String TASK_DELETE_CANT_DELETE;

    @Value("${workflow.loop}")
    public String WORKFLOW_LOOP;
}
