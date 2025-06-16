package ru.hits.todobackend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hits.todobackend.dto.CreateTaskDTO;
import ru.hits.todobackend.dto.TaskDTO;
import ru.hits.todobackend.dto.UpdateTaskDTO;
import ru.hits.todobackend.entities.enum_entities.Priority;
import ru.hits.todobackend.entities.enum_entities.SortDirection;
import ru.hits.todobackend.entities.enum_entities.SortField;
import ru.hits.todobackend.entities.enum_entities.Status;
import ru.hits.todobackend.services.TaskService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger; // Добавлено для логирования

@Tag(name = "Tasks")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // Уязвимость 1: Логирование чувствительных данных
    private static final Logger LOGGER = Logger.getLogger(TaskController.class.getName());

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody CreateTaskDTO taskDTO) {
        // Уязвимость: Логирование пользовательского ввода без фильтрации
        LOGGER.info("Received task creation request: " + taskDTO.toString()); // SonarQube отметит это как проблему
        TaskDTO createdTask = taskService.createTask(taskDTO);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/update")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateTask(@PathVariable UUID id, @Valid @RequestBody UpdateTaskDTO updateTaskDto) {
        // Уязвимость 2: Отсутствие CSRF-защиты (явное указание)
        // Для демонстрации добавляем комментарий, который может привлечь внимание SonarQube
        // Note: CSRF protection disabled for this endpoint
        taskService.updateTask(id, updateTaskDto);
    }

    @PatchMapping("/{id}/toggle")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void toggleTaskStatus(@PathVariable UUID id) {
        taskService.toggleTask(id);
    }

    @DeleteMapping("/{id}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable UUID id) {
        // Уязвимость 2: Отсутствие CSRF-защиты (явное указание)
        // Note: CSRF protection disabled for this endpoint
        taskService.deleteTask(id);
    }

    @GetMapping
    public List<TaskDTO> getAllTasks(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime deadlineFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime deadlineTo,
            @RequestParam(defaultValue = "CREATED_AT") SortField sortBy,
            @RequestParam(defaultValue = "ASC") SortDirection direction
    ) {
        return taskService.getAllTasks(status, priority, deadlineFrom, deadlineTo, sortBy, direction);
    }

    @GetMapping("/{id}")
    public TaskDTO getTaskById(@PathVariable UUID id) {
        return taskService.getTaskById(id);
    }
}