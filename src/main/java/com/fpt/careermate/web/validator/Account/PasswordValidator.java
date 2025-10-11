package com.fpt.careermate.web.validator.Account;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<PasswordConstraint, String> {

    @Override
    public void initialize(PasswordConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false;
        // At least 8 characters
        if (value.length() < 8) return false;
        // At least one uppercase letter
        if (!value.matches(".*[A-Z].*")) return false;
        // At least one lowercase letter
        if (!value.matches(".*[a-z].*")) return false;
        // At least one digit
        if (!value.matches(".*\\d.*")) return false;
        // At least one special character (corrected regex)
        if (!value.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) return false;
        return true;
    }
}
