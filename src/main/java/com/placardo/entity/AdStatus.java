package com.placardo.entity;

public enum AdStatus {
    PENDING("На модерации"),
    ACTIVE("Активно"),
    REJECTED("Отклонено"),
    ARCHIVED("В архиве");

    private final String label;

    AdStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
