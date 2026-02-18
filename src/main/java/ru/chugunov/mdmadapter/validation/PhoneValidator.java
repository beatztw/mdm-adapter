package ru.chugunov.mdmadapter.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import static ru.chugunov.mdmadapter.utils.Constants.RUS_PHONE_PATTERN;

public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (!StringUtils.hasText(value)) {
            return true;
        }

        return value.matches(RUS_PHONE_PATTERN);
    }
}
