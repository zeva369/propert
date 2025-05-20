package com.sidus.propert.validation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sidus.propert.validation.validator.ExistProjectValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = ExistProjectValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExistProject {
    String message() default "Project does not exist";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}