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
public class TaskDTO {

    @NotNull
    UUID id;

    @NotNull
    String title;

    @NotNull
    String description;

    @NotNull
    Status status;

    @NotNull
    Priority priority;

    @NotNull
    OffsetDateTime deadline;

    @NotNull
    OffsetDateTime createdAt;

    @NotNull
    OffsetDateTime updatedAt;
}

