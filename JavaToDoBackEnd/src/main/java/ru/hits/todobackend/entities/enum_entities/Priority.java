package ru.hits.todobackend.entities.enum_entities;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Приоритет задачи", enumAsRef = true)
public enum Priority {
    CRITICAL, HIGH, MEDIUM, LOW
}
