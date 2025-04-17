package com.seva.propert.validation.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.seva.propert.model.entity.Project;
import com.seva.propert.service.ProjectService;
import com.seva.propert.validation.annotation.ExistProject;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExistProjectValidator implements ConstraintValidator<ExistProject, Project> {

   private static ApplicationContext applicationContext;

    private ProjectService getProjectService() {
        return applicationContext.getBean(ProjectService.class);
    }
    
    @Override
    public void initialize(ExistProject constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Project project, ConstraintValidatorContext context) {
        // If projectId is null, then is invalid
        if (project == null || project.getId() == null) {
            return false;
        }

        return getProjectService().findById(project.getId()).isPresent();
    }

    
}