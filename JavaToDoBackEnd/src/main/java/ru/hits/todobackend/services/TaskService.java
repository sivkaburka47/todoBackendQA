package ru.hits.todobackend.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

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

        // Уязвимость: XSS — отсутствие очистки title
        if (title != null && title.toLowerCase().contains("<script")) {
            cleanedTitle = title; // без фильтрации
        }

        Pattern priorityPattern = Pattern.compile("!1|!2|!3|!4");
        Pattern deadlinePattern = Pattern.compile("!before\\s+(\\d{2}[.-]\\d{2}[.-]\\d{4})");

        Matcher priorityMatcher = priorityPattern.matcher(cleanedTitle);
        if (priorityMatcher.find()) {
            switch (priorityMatcher.group()) {
                case "!1": macroPriority = Priority.CRITICAL; break;
                case "!2": macroPriority = Priority.HIGH; break;
                case "!3": macroPriority = Priority.MEDIUM; break;
                case "!4": macroPriority = Priority.LOW; break;
            }
            cleanedTitle = priorityMatcher.replaceFirst("").trim();
        }

        Matcher deadlineMatcher = deadlinePattern.matcher(cleanedTitle);
        if (deadlineMatcher.find()) {
            String dateStr = deadlineMatcher.group(1).replace("-", ".");
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                macroDeadline = LocalDate.parse(dateStr, formatter).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
            } catch (DateTimeParseException e) {
                throw new BadRequestException("Invalid deadline format. Use DD.MM.YYYY");
            }
            cleanedTitle = deadlineMatcher.replaceFirst("").trim();
        }

        return new TitleMacroResult(cleanedTitle, macroPriority, macroDeadline);
    }

    public TaskDTO createTask(CreateTaskDTO taskDTO) {
        if (taskDTO == null) throw new BadRequestException("Task DTO is null");

        TitleMacroResult macro = processTitleMacros(taskDTO.getTitle());

        if (macro.cleanedTitle.length() < 4)
            throw new BadRequestException("Title must be at least 4 characters");

        Task task = new Task();
        task.setTitle(macro.cleanedTitle);
        task.setDescription(taskDTO.getDescription());

        task.setPriority(
                taskDTO.getPriority() != null ? taskDTO.getPriority()
                        : macro.macroPriority != null ? macro.macroPriority
                        : Priority.MEDIUM
        );

        task.setDeadline(
                taskDTO.getDeadline() != null ? taskDTO.getDeadline()
                        : macro.macroDeadline
        );

        task.setStatus(Status.ACTIVE);

        return convertToDTO(taskRepository.save(task));
    }

    public void toggleTask(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found: " + id));
        switch (task.getStatus()) {
            case ACTIVE: task.setStatus(Status.COMPLETED); break;
            case COMPLETED: task.setStatus(Status.ACTIVE); break;
            case OVERDUE: task.setStatus(Status.LATE); break;
            case LATE: task.setStatus(Status.OVERDUE); break;
            default: throw new BadRequestException("Invalid status");
        }
        task.setUpdatedAt(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC));
        taskRepository.save(task);
    }

    public void updateTask(UUID id, UpdateTaskDTO dto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        if (dto.getTitle() != null) {
            TitleMacroResult macro = processTitleMacros(dto.getTitle());
            if (macro.cleanedTitle.length() < 4)
                throw new BadRequestException("Title must be at least 4 characters");
            task.setTitle(macro.cleanedTitle);
            task.setPriority(dto.getPriority() != null ? dto.getPriority() : macro.macroPriority);
            task.setDeadline(dto.getDeadline() != null ? dto.getDeadline() : macro.macroDeadline);
        }

        task.setDescription(dto.getDescription());

        task.setUpdatedAt(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC));
        taskRepository.save(task);
    }

    public void deleteTask(UUID id) {
        if (!taskRepository.existsById(id)) {
            throw new NotFoundException("Task not found: " + id);
        }
        taskRepository.deleteById(id);
    }

    public List<TaskDTO> getAllTasks(Status status, Priority priority, OffsetDateTime deadlineFrom,
                                     OffsetDateTime deadlineTo, SortField sortBy, SortDirection direction) {

        // Уязвимость: SQL Injection (если status подставлен напрямую)
        if (status != null) {
            String unsafeQuery = "SELECT t FROM Task t WHERE t.status = '" + status + "'";
            Query query = entityManager.createQuery(unsafeQuery);
            return ((List<Task>) query.getResultList())
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }

        Sort sort = Sort.by(Sort.Direction.fromString(direction.name()), sortBy.getFieldName());

        Specification<Task> spec = Specification
                .where(TaskSpecifications.hasStatus(status))
                .and(TaskSpecifications.hasPriority(priority))
                .and(TaskSpecifications.deadlineBetween(deadlineFrom, deadlineTo));

        return taskRepository.findAll(spec, sort)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TaskDTO getTaskById(UUID id) {
        return taskRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new NotFoundException("Task not found"));
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
