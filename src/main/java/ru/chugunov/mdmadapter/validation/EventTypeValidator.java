package ru.chugunov.mdmadapter.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import static ru.chugunov.mdmadapter.utils.Constants.EXPECTED_EVENT_TYPE;

public class EventTypeValidator implements ConstraintValidator<ValidEventType, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (!StringUtils.hasText(value)) {
            return true;
        }

        return value.matches(EXPECTED_EVENT_TYPE);
    }
}
