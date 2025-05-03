package ru.hits.todobackend.entities.enum_entities;

public enum SortField {
    TITLE("title"),
    STATUS("status"),
    PRIORITY("priority"),
    DEADLINE("deadline"),
    CREATED_AT("createdAt"),
    UPDATED_AT("updatedAt");

    private final String fieldName;

    SortField(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
