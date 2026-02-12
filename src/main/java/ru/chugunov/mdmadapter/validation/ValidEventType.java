package ru.chugunov.mdmadapter.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EventTypeValidator.class)
public @interface ValidEventType {

    String message() default "Тип события должен быть USER_PHONE_CHANGE";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
