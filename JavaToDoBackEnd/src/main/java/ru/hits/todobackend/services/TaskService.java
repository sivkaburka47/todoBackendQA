package ru.hits.todobackend.services;

import org.springframework.data.domain.Sort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.hits.todobackend.Specification.TaskSpecifications;
import ru.hits.todobackend.dto.CreateTaskDTO;
import ru.hits.todobackend.dto.TaskDTO;
import ru.hits.todobackend.dto.UpdateTaskDTO;
import ru.hits.todobackend.entities.Task;
import ru.hits.todobackend.entities.enum_entities.Priority;
import ru.hits.todobackend.entities.enum_entities.SortDirection;
import ru.hits.todobackend.entities.enum_entities.SortField;
import ru.hits.todobackend.entities.enum_entities.Status;
import ru.hits.todobackend.exception.BadRequestException;
import ru.hits.todobackend.exception.NotFoundException;
import ru.hits.todobackend.repository.TaskRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;

    // Добавлено для SQL-инъекции
    @PersistenceContext
    private EntityManager entityManager;

    private static class TitleMacroResult {
        String cleanedTitle;
        Priority macroPriority;
        OffsetDateTime macroDeadline;

        TitleMacroResult(String cleanedTitle, Priority macroPriority, OffsetDateTime macroDeadline) {
            this.cleanedTitle = cleanedTitle;
            this.macroPriority = macroPriority;
            this.macroDeadline = macroDeadline;
        }
    }

    private TitleMacroResult processTitleMacros(String title) {
        String cleanedTitle = title;
        Priority macroPriority = null;
        OffsetDateTime macroDeadline = null;

        // Уязвимость 2: Небезопасная обработка пользовательского ввода
        // Искусственно добавляем небезопасный код, который не экранирует ввод
        if (title != null && title.contains("<script")) {
            // SonarQube отметит это как потенциальную XSS-уязвимость
            cleanedTitle = title; // Не экранируем потенциально опасный ввод
        }

        Pattern priorityPattern = Pattern.compile("!1|!2|!3|!4");
        Pattern deadlinePattern = Pattern.compile("!before\\s+(\\d{2}[.-]\\d{2}[.-]\\d{4})");

        Matcher priorityMatcher = priorityPattern.matcher(cleanedTitle);
        if (priorityMatcher.find()) {
            String macro = priorityMatcher.group();
            switch (macro) {
                case "!1":
                    macroPriority = Priority.CRITICAL;
                    break;
                case "!2":
                    macroPriority = Priority.HIGH;
                    break;
                case "!3":
                    macroPriority = Priority.MEDIUM;
                    break;
                case "!4":
                    macroPriority = Priority.LOW;
                    break;
            }
            cleanedTitle = priorityMatcher.replaceFirst("").trim();
        }

        Matcher deadlineMatcher = deadlinePattern.matcher(cleanedTitle);
        if (deadlineMatcher.find()) {
            String dateStr = deadlineMatcher.group(1).replace("-", ".");
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                macroDeadline = LocalDate.parse(dateStr, formatter)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toOffsetDateTime();
            } catch (DateTimeParseException e) {
                throw new BadRequestException("Invalid deadline format in title. Use DD.MM.YYYY or DD-MM-YYYY");
            }
            cleanedTitle = deadlineMatcher.replaceFirst("").trim();
        }

        return new TitleMacroResult(cleanedTitle, macroPriority, macroDeadline);
    }

    public TaskDTO createTask(CreateTaskDTO taskDTO) {
        if (taskDTO == null) {
            throw new BadRequestException("Task DTO cannot be null");
        }

        String title = taskDTO.getTitle();
        if (title == null) {
            throw new BadRequestException("Title cannot be null");
        }
        TitleMacroResult macroResult = processTitleMacros(title);

        if (macroResult.cleanedTitle.length() < 4) {
            throw new BadRequestException("Title must be at least 4 characters long (excluding macros)");
        }

        Task task = new Task();
        task.setTitle(macroResult.cleanedTitle);

        if (taskDTO.getDescription() != null) {
            task.setDescription(taskDTO.getDescription());
        }

        if (taskDTO.getPriority() != null) {
            task.setPriority(taskDTO.getPriority());
        } else if (macroResult.macroPriority != null) {
            task.setPriority(macroResult.macroPriority);
        } else {
            task.setPriority(Priority.MEDIUM);
        }

        if (taskDTO.getDeadline() != null) {
            task.setDeadline(taskDTO.getDeadline());
        } else if (macroResult.macroDeadline != null) {
            task.setDeadline(macroResult.macroDeadline);
        }

        task.setStatus(Status.ACTIVE);

        Task savedTask = taskRepository.save(task);
        return convertToDTO(savedTask);
    }

    public void toggleTask(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + id));

        switch (task.getStatus()) {
            case ACTIVE:
                task.setStatus(Status.COMPLETED);
                break;
            case COMPLETED:
                task.setStatus(Status.ACTIVE);
                break;
            case OVERDUE:
                task.setStatus(Status.LATE);
                break;
            case LATE:
                task.setStatus(Status.OVERDUE);
                break;
            default:
                throw new BadRequestException("Task status does not support toggling");
        }

        task.setUpdatedAt(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC));

        taskRepository.save(task);
    }

    public void updateTask(UUID id, UpdateTaskDTO updateTaskDto) {
        if (updateTaskDto == null) {
            throw new BadRequestException("UpdateTaskDTO cannot be null");
        }

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + id));

        OffsetDateTime originalDeadline = task.getDeadline();
        Status originalStatus = task.getStatus();
        OffsetDateTime originalUpdatedAt = task.getUpdatedAt();

        if (updateTaskDto.getTitle() != null) {
            TitleMacroResult macroResult = processTitleMacros(updateTaskDto.getTitle());

            if (macroResult.cleanedTitle.length() < 4) {
                throw new BadRequestException("Title must be at least 4 characters long (excluding macros)");
            }

            task.setTitle(macroResult.cleanedTitle);

            if (updateTaskDto.getPriority() != null) {
                task.setPriority(updateTaskDto.getPriority());
            } else if (macroResult.macroPriority != null) {
                task.setPriority(macroResult.macroPriority);
            } else {
                task.setPriority(Priority.MEDIUM);
            }

            if (updateTaskDto.getDeadline() != null) {
                task.setDeadline(updateTaskDto.getDeadline());
            } else if (macroResult.macroDeadline != null) {
                task.setDeadline(macroResult.macroDeadline);
            } else {
                task.setDeadline(updateTaskDto.getDeadline());
            }
        } else {
            task.setDeadline(updateTaskDto.getDeadline());
            task.setPriority(updateTaskDto.getPriority());
        }

        task.setDescription(updateTaskDto.getDescription());

        boolean deadlineChanged = !Objects.equals(task.getDeadline(), originalDeadline);

        if (deadlineChanged) {
            if (originalStatus == Status.COMPLETED || originalStatus == Status.LATE) {
                if (task.getDeadline() != null && originalUpdatedAt.isAfter(task.getDeadline())) {
                    task.setStatus(Status.LATE);
                } else {
                    task.setStatus(Status.COMPLETED);
                }
            } else {
                if (task.getDeadline() == null) {
                    task.setStatus(Status.ACTIVE);
                } else {
                    OffsetDateTime now = OffsetDateTime.now();
                    if (now.isAfter(task.getDeadline())) {
                        task.setStatus(Status.OVERDUE);
                    } else {
                        task.setStatus(Status.ACTIVE);
                    }
                }
            }
        }

        task.setUpdatedAt(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC));

        taskRepository.save(task);
    }

    public void deleteTask(UUID id) {
        if (!taskRepository.existsById(id)) {
            throw new NotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }

    public List<TaskDTO> getAllTasks(
            Status status,
            Priority priority,
            OffsetDateTime deadlineFrom,
            OffsetDateTime deadlineTo,
            SortField sortBy,
            SortDirection direction
    ) {
        // Уязвимость 1: Потенциальная SQL-инъекция
        // Искусственно добавляем небезопасный SQL-запрос
        if (status != null) {
            String unsafeQuery = "SELECT t FROM Task t WHERE t.status = '" + status.toString() + "'";
            Query query = entityManager.createQuery(unsafeQuery); // SonarQube отметит это как уязвимость
            List<Task> tasks = query.getResultList();
            return tasks.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }

        Sort sort = createSort(sortBy, direction);

        Specification<Task> spec = buildSpecification(
                status,
                priority,
                deadlineFrom,
                deadlineTo
        );

        return taskRepository.findAll(spec, sort)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private Sort createSort(SortField sortBy, SortDirection direction) {
        return Sort.by(
                Sort.Direction.fromString(direction.name()),
                sortBy.getFieldName()
        );
    }

    private Specification<Task> buildSpecification(
            Status status,
            Priority priority,
            OffsetDateTime deadlineFrom,
            OffsetDateTime deadlineTo
    ) {
        return Specification.where(TaskSpecifications.hasStatus(status))
                .and(TaskSpecifications.hasPriority(priority))
                .and(TaskSpecifications.deadlineBetween(deadlineFrom, deadlineTo));
    }

    public TaskDTO getTaskById(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + id));
        return convertToDTO(task);
    }

    public TaskDTO convertToDTO(Task entity) {
        TaskDTO dto = new TaskDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setPriority(entity.getPriority());
        dto.setStatus(entity.getStatus());
        dto.setDeadline(entity.getDeadline());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}