package ru.hits.todobackend.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
import ru.hits.todobackend.services.XmlProcessorService;

import java.io.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Tag(name = "Tasks")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final XmlProcessorService xmlProcessorService;

    private static final Logger LOGGER = Logger.getLogger(TaskController.class.getName());

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody CreateTaskDTO taskDTO, HttpServletRequest request) {
        // Уязвимость: логирование пользовательских данных
        String ip = request.getRemoteAddr();
        LOGGER.info("Task from IP " + ip + ": " + taskDTO.toString());

        TaskDTO createdTask = taskService.createTask(taskDTO);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/update")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateTask(@PathVariable UUID id, @Valid @RequestBody UpdateTaskDTO updateTaskDto) {
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
        LOGGER.warning("Deleting task with ID: " + id);
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

    // Уязвимость: Path Traversal
    @GetMapping("/read-file")
    public ResponseEntity<String> readFile(@RequestParam String filename) {
        File file = new File("/var/data/tasks/" + filename);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return ResponseEntity.ok(sb.toString());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error reading file: " + e.getMessage());
        }
    }

    // Уязвимость: XXE
    @PostMapping("/parse-xml")
    public ResponseEntity<String> parseXml(@RequestBody String xml) {
        return ResponseEntity.ok(xmlProcessorService.parseXml(xml));
    }
}
