package ru.hits.todobackend.services;

import jakarta.validation.Valid;
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskDTO createTask(CreateTaskDTO taskDTO) {
        if (taskDTO.getDeadline().isBefore(OffsetDateTime.now())) {
            throw new BadRequestException("Deadline cannot be before the current time");
        }

        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setPriority(taskDTO.getPriority());
        task.setDeadline(taskDTO.getDeadline());
        task.setStatus(Status.ACTIVE);

        Task savedTask = taskRepository.save(task);
        return convertToDTO(savedTask);
    }

    public TaskDTO updateTask(UUID id, UpdateTaskDTO updateTaskDto) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + id));

        if (updateTaskDto.getDeadline() != null
                && updateTaskDto.getDeadline().isBefore(task.getCreatedAt())) {
            throw new BadRequestException("Deadline cannot be before creation time");
        }


        if (updateTaskDto.getTitle() != null) {
            task.setTitle(updateTaskDto.getTitle());
        }
        if (updateTaskDto.getDescription() != null) {
            task.setDescription(updateTaskDto.getDescription());
        }
        if (updateTaskDto.getStatus() != null) {
            task.setStatus(updateTaskDto.getStatus());
        }
        if (updateTaskDto.getPriority() != null) {
            task.setPriority(updateTaskDto.getPriority());
        }
        if (updateTaskDto.getDeadline() != null) {
            task.setDeadline(updateTaskDto.getDeadline());
        }

        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
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
        // Создаем объект сортировки
        Sort sort = createSort(sortBy, direction);

        // Строим спецификацию для фильтрации
        Specification<Task> spec = buildSpecification(
                status,
                priority,
                deadlineFrom,
                deadlineTo
        );

        // Выполняем запрос с фильтрацией и сортировкой
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

    private TaskDTO convertToDTO(Task entity) {
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
