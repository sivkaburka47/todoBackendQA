package ru.hits.todobackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.hits.todobackend.entities.enum_entities.Priority;
import ru.hits.todobackend.entities.enum_entities.Status;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateTaskDTO {
    String title;
    String description;
    Priority priority;
    OffsetDateTime deadline;
}