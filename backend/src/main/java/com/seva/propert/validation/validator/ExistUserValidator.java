package com.seva.propert.validation.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.seva.propert.model.entity.User;
import com.seva.propert.service.UserService;
import com.seva.propert.validation.annotation.ExistUser;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ExistUserValidator implements ConstraintValidator<ExistUser, User> {

   // private static ApplicationContext applicationContext;

   private static ApplicationContext applicationContext;

    @Autowired
    public void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    private UserService getUserService() {
        return applicationContext.getBean(UserService.class);
    }
    
    @Override
    public void initialize(ExistUser constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(User user, ConstraintValidatorContext context) {
        // If projectId is null, then is invalid
        if (user == null || user.getId() == null) {
            return false;
        }
        if (getUserService().findById(user.getId()).isEmpty()) return false;
      
        return true;
    }

    
}