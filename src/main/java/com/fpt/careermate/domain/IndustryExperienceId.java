package com.fpt.careermate.domain;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IndustryExperienceId implements Serializable {
    String fieldName;
    Integer candidateId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndustryExperienceId that = (IndustryExperienceId) o;
        return Objects.equals(fieldName, that.fieldName) && Objects.equals(candidateId, that.candidateId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldName, candidateId);
    }
}

