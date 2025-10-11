package com.fpt.careermate.web.validator.CandidateProfile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class DobValidator implements ConstraintValidator<DobConstraint, LocalDate> {
    private int minAge;

    @Override
    public void initialize(DobConstraint constraintAnnotation) {
        this.minAge = constraintAnnotation.min();
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) return false;
        LocalDate today = LocalDate.now();
        LocalDate minDob = today.minusYears(minAge);
        return value.isBefore(minDob) || value.isEqual(minDob);
    }
}
