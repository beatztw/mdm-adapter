package ru.chugunov.mdmadapter.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import static ru.chugunov.mdmadapter.utils.Constants.GUID_PATTERN;

public class GuidValidator implements ConstraintValidator<ValidGuid, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (!StringUtils.hasText(value)) {
            return true;
        }

        return value.matches(GUID_PATTERN);
    }
}
