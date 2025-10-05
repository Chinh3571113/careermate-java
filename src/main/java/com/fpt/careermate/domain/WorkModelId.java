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
public class WorkModelId implements Serializable {
    String name;
    Integer candidateId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkModelId that = (WorkModelId) o;
        return Objects.equals(name, that.name) && Objects.equals(candidateId, that.candidateId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, candidateId);
    }
}
