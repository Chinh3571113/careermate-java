package com.fpt.careermate.common.constant;

public enum WorkModel {
    AT_OFFICE("At Office"),
    REMOTE("Remote"),
    HYBRID("Hybrid");

    private final String displayName;

    WorkModel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

