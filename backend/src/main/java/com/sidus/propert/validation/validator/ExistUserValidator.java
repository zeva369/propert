package com.sidus.propert.validation.validator;

import com.sidus.propert.model.entity.User;
import com.sidus.propert.service.UserService;
import com.sidus.propert.validation.annotation.ExistUser;
import org.springframework.context.ApplicationContext;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExistUserValidator implements ConstraintValidator<ExistUser, User> {

    private static ApplicationContext applicationContext;

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
        return getUserService().findById(user.getId()).isPresent();
 
    }

    
}