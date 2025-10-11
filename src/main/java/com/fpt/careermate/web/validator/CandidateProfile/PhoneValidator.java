package com.fpt.careermate.web.validator.CandidateProfile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneValidator implements ConstraintValidator<PhoneConstraint, String> {
    @Override
    public void initialize(PhoneConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) return false;
        // Phone number must have exactly 10 digits (>9 and <11)
        if (value.length() != 10) return false;
        // All characters must be digits
        return value.matches("\\d{10}");
    }
}
