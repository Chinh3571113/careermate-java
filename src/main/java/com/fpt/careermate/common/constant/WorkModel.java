package com.fpt.careermate.common.constant;

public enum WorkModel {
    AT_OFFICE("AT OFFICE"),
    REMOTE("REMOTE"),
    HYBRID("HYBRID");

    private final String displayName;

    WorkModel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

