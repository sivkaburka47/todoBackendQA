package ru.hits.todobackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.hits.todobackend.entities.enum_entities.Priority;

import java.time.OffsetDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateTaskDTO {
    @NotNull
    String title;

    @NotNull
    String description;

    @NotNull
    Priority priority;

    @NotNull
    OffsetDateTime deadline;

}
