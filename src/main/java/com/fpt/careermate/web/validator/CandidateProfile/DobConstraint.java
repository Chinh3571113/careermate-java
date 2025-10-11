package com.fpt.careermate.web.validator.CandidateProfile;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = {DobValidator.class })
public @interface DobConstraint {
    String message() default "Date of birth must be at least {min} years ago";
    int min() default 18;
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
