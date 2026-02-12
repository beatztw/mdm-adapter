package ru.chugunov.mdmadapter.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = GuidValidator.class)
public @interface ValidGuid {

    String message() default "guid должен содержать ровно 32 символа";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
