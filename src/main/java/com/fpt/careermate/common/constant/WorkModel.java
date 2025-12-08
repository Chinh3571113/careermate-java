package com.fpt.careermate.common.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum WorkModel {
    AT_OFFICE("Onsite"),
    REMOTE("Remote"),
    HYBRID("Hybrid");

    private final String displayName;

    WorkModel(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static WorkModel fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (WorkModel model : WorkModel.values()) {
            if (model.displayName.equalsIgnoreCase(value) || model.name().equalsIgnoreCase(value)) {
                return model;
            }
        }
        throw new IllegalArgumentException("Unknown WorkModel: " + value);
    }
}

