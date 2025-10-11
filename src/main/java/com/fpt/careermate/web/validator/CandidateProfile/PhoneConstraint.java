package com.fpt.careermate.web.validator.CandidateProfile;

import jakarta.validation.Payload;

public @interface PhoneConstraint {
    String message() default "Invalid password";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
