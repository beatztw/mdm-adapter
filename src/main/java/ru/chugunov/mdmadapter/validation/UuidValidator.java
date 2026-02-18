package ru.chugunov.mdmadapter.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import static ru.chugunov.mdmadapter.utils.Constants.UUID_PATTERN;

public class UuidValidator implements ConstraintValidator<ValidUUID, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (!StringUtils.hasText(value)) {
            return true;
        }

        return value.matches(UUID_PATTERN);
    }
}
