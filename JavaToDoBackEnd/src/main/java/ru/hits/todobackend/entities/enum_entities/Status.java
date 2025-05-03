package ru.hits.todobackend.entities.enum_entities;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статус задачи", enumAsRef = true)
public enum Status {
    ACTIVE, COMPLETED, OVERDUE, LATE
}

