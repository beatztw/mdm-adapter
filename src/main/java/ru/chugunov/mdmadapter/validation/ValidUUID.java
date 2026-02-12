package ru.chugunov.mdmadapter.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UuidValidator.class)
public @interface ValidUUID {

    String message() default "Не соответствует формату UUID";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
