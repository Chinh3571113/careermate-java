package com.fpt.careermate.common.constant;

import lombok.Getter;

@Getter
public enum FeedbackType {
    LIKE("like"),
    DISLIKE("dislike"),
    SAVE("save"),
    APPLY("apply"),
    VIEW("view");

    private final String value;

    FeedbackType(String value) {
        this.value = value;
    }

    public static FeedbackType fromValue(String value) {
        for (FeedbackType type : FeedbackType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid feedback type: " + value);
    }
}

