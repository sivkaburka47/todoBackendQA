package ru.hits.todobackend.servicesTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.hits.todobackend.dto.CreateTaskDTO;
import ru.hits.todobackend.dto.TaskDTO;
import ru.hits.todobackend.dto.UpdateTaskDTO;
import ru.hits.todobackend.entities.Task;
import ru.hits.todobackend.entities.enum_entities.Priority;

import ru.hits.todobackend.entities.enum_entities.Status;
import ru.hits.todobackend.exception.BadRequestException;
import ru.hits.todobackend.exception.NotFoundException;
import ru.hits.todobackend.repository.TaskRepository;
import ru.hits.todobackend.services.TaskService;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private CreateTaskDTO baseDto;
    private UpdateTaskDTO updateTaskDTO;
    private Task existingTask;
    private UUID taskId;

    private Task task1;
    private TaskDTO taskDTO1;




    @BeforeEach
    void setup() {
        baseDto = new CreateTaskDTO();
        baseDto.setTitle("Valid title");
        baseDto.setDeadline(OffsetDateTime.now());

        updateTaskDTO = new UpdateTaskDTO();
        taskId = UUID.randomUUID();
        existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setTitle("Original title");
        existingTask.setPriority(Priority.MEDIUM);
        existingTask.setStatus(Status.ACTIVE);
        existingTask.setCreatedAt(OffsetDateTime.now().minusDays(1));
        existingTask.setUpdatedAt(OffsetDateTime.now().minusDays(1));

    }


    //func createTasks Tests

    @Test
    @DisplayName("�������� ������ � �������� ���������� � ��� ��������")
    void testCreateTask_ValidInput_NoMacros() {
        Task saved = new Task();
        saved.setId(UUID.randomUUID());
        saved.setTitle("Valid title");
        saved.setPriority(Priority.MEDIUM);
        saved.setStatus(Status.ACTIVE);

        when(taskRepository.save(any())).thenReturn(saved);

        TaskDTO result = taskService.createTask(baseDto);

        assertNotNull(result);
        assertEquals("Valid title", result.getTitle());
        assertEquals(Priority.MEDIUM, result.getPriority());
        verify(taskRepository).save(any());
    }

    @Test
    @DisplayName("������ ��� null DTO")
    void testCreateTask_NullDTO_ThrowsException() {
        assertThrows(BadRequestException.class, () -> taskService.createTask(null));
    }

    @Test
    @DisplayName("������ ��� null title")
    void testCreateTask_NullTitle_ThrowsException() {
        baseDto.setTitle(null);
        assertThrows(BadRequestException.class, () -> taskService.createTask(baseDto));
    }

    @Test
    @DisplayName("������ ��� �������� �������� (������ 4)")
    void testCreateTask_ShortTitle_ThrowsException() {
        baseDto.setTitle("aff");
        assertThrows(BadRequestException.class, () -> taskService.createTask(baseDto));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "!1 Task with critical priority",
            "!2 Task with high priority",
            "!3 Task with medium priority",
            "!4 Task with low priority"
    })
    @DisplayName("��������� ���������� ����� ������� !1-!4")
    void testCreateTask_PriorityMacro(String title) {
        baseDto.setTitle(title);
        Task saved = new Task();
        saved.setId(UUID.randomUUID());
        saved.setTitle(title.replaceFirst("!\\d", "").trim());

        when(taskRepository.save(any())).thenReturn(saved);

        TaskDTO result = taskService.createTask(baseDto);
        assertNotNull(result);
        assertEquals(saved.getTitle(), result.getTitle());
    }

    @Test
    @DisplayName("��������� ���������� �� ���� ����� ��������� ��� ��������")
    void testCreateTask_FieldPriorityOverridesMacro() {
        baseDto.setTitle("!1 Critical but overridden");
        baseDto.setPriority(Priority.LOW);

        Task saved = new Task();
        saved.setId(UUID.randomUUID());
        saved.setTitle("Critical but overridden");
        saved.setPriority(Priority.LOW);

        when(taskRepository.save(any())).thenReturn(saved);

        TaskDTO result = taskService.createTask(baseDto);
        assertEquals(Priority.LOW, result.getPriority());
    }

    @Test
    @DisplayName("��������� �������� ����� ������ !before")
    void testCreateTask_DeadlineMacro() {
        baseDto.setTitle("!before 01.01.2030 Finish this");

        Task saved = new Task();
        saved.setId(UUID.randomUUID());
        saved.setTitle("Finish this");

        when(taskRepository.save(any())).thenReturn(saved);

        TaskDTO result = taskService.createTask(baseDto);
        assertEquals("Finish this", result.getTitle());
    }

    @Test
    @DisplayName("������ ��� �������� ������� �������� � �������")
    void testCreateTask_InvalidDeadlineMacro_ThrowsException() {
        baseDto.setTitle("!before 99.99.9999 Invalid date");
        assertThrows(BadRequestException.class, () -> taskService.createTask(baseDto));
    }

    @Test
    @DisplayName("������ ��� �������� � ������������ ����� (����� > 12) ")
    void testCreateTask_InvalidMonthInDeadlineMacro_ThrowsException() {
        baseDto.setTitle("!before 31.13.2025 Invalid month");
        assertThrows(BadRequestException.class, () -> taskService.createTask(baseDto));
    }

    @Test
    @DisplayName("������ ��� �������� � ������������ ����� (����� > 12) � - ������ .")
    void testCreateTask_InvalidMonthInDeadlineDashMacro_ThrowsException() {
        baseDto.setTitle("!before 31-13-2025 Invalid month");
        assertThrows(BadRequestException.class, () -> taskService.createTask(baseDto));
    }

    @Test
    @DisplayName("������ ��� �������� � ������������ ����� (���� > 31)")
    void testCreateTask_InvalidDayInDeadlineMacro_ThrowsException() {
        baseDto.setTitle("!before 32.12.2025 Invalid day");
        assertThrows(BadRequestException.class, () -> taskService.createTask(baseDto));
    }

    @Test
    @DisplayName("������ ��� �������� � ������������ ����� (���� > 31) � - ������ .")
    void testCreateTask_InvalidDayInDeadlineDashMacro_ThrowsException() {
        baseDto.setTitle("!before 32-12-2025 Invalid day");
        assertThrows(BadRequestException.class, () -> taskService.createTask(baseDto));
    }

    @Test
    @DisplayName("������ ��� ���� � ������ (00.00.0000)")
    void testCreateTask_ZeroDateInMacro_ThrowsException() {
        baseDto.setTitle("!before 00.00.0000 Zero date");
        assertThrows(BadRequestException.class, () -> taskService.createTask(baseDto));
    }

    @Test
    @DisplayName("������ ��� ���� � ������ (00.00.0000) � - ������ .")
    void testCreateTask_ZeroDateInMacroDash_ThrowsException() {
        baseDto.setTitle("!before 00-00-0000 Zero date");
        assertThrows(BadRequestException.class, () -> taskService.createTask(baseDto));
    }

    @Test
    @DisplayName("������ ��� ������� ������ ���� (��������, 01.01.0000)")
    void testCreateTask_TooEarlyDateInMacro_ThrowsException() {
        baseDto.setTitle("!before 01.01.0000 Too early date");
        assertThrows(BadRequestException.class, () -> taskService.createTask(baseDto));
    }

    @Test
    @DisplayName("������ ��� ������� ������ ���� (��������, 01.01.0000)  � - ������ .")
    void testCreateTask_TooEarlyDateInMacroDash_ThrowsException() {
        baseDto.setTitle("!before 01-01-0000 Too early date");
        assertThrows(BadRequestException.class, () -> taskService.createTask(baseDto));
    }


    @Test
    @DisplayName("���� �������� ������� ����������� ������")
    void testCreateTask_FieldDeadlineOverridesMacro() {
        baseDto.setTitle("!before 01.01.2030 Finish this");

        Task saved = new Task();
        saved.setId(UUID.randomUUID());
        saved.setTitle("Finish this");
        saved.setDeadline(baseDto.getDeadline());

        when(taskRepository.save(any())).thenReturn(saved);

        TaskDTO result = taskService.createTask(baseDto);
        assertEquals("Finish this", result.getTitle());
        assertEquals(baseDto.getDeadline(), result.getDeadline());
    }

    @Test
    @DisplayName("������������� ������� � ������� !before")
    void testCreateTask_DeadlineMacroWithDashes() {
        baseDto.setTitle("!before 01-01-2030 Start task");

        Task saved = new Task();
        saved.setId(UUID.randomUUID());
        saved.setTitle("Start task");

        when(taskRepository.save(any())).thenReturn(saved);

        TaskDTO result = taskService.createTask(baseDto);
        assertEquals("Start task", result.getTitle());
    }

    @Test
    @DisplayName("�������� ��������� ��������")
    void testCreateTask_WithDescription() {
        baseDto.setDescription("Detailed description");

        Task saved = new Task();
        saved.setId(UUID.randomUUID());
        saved.setDescription("Detailed description");

        when(taskRepository.save(any())).thenReturn(saved);

        TaskDTO result = taskService.createTask(baseDto);
        assertEquals("Detailed description", result.getDescription());
    }

    @Test
    @DisplayName("�������� � ����������� ���������: ��� �����������")
    void testCreateTask_WithPriorityAndDeadlineMacros() {
        baseDto.setTitle("!2 !before 01.12.2030 Combo title");

        Task saved = new Task();
        saved.setId(UUID.randomUUID());
        saved.setTitle("Combo title");

        when(taskRepository.save(any())).thenReturn(saved);

        TaskDTO result = taskService.createTask(baseDto);
        assertEquals("Combo title", result.getTitle());
    }

    @Test
    @DisplayName("������� � �������� ������ � ���� ��������������")
    void testCreateTask_MacrosInMiddleOfTitle() {
        baseDto.setTitle("Task !before 01.12.2030 is due");

        Task saved = new Task();
        saved.setId(UUID.randomUUID());
        saved.setTitle("Task is due");

        when(taskRepository.save(any())).thenReturn(saved);

        TaskDTO result = taskService.createTask(baseDto);
        assertEquals("Task is due", result.getTitle());
    }

    @Test
    @DisplayName("�������� ������ ��� �������� � ���������� � ����������� default ��������")
    void testCreateTask_UseDefaults() {
        baseDto.setTitle("Simple title");

        Task saved = new Task();
        saved.setId(UUID.randomUUID());
        saved.setTitle("Simple title");
        saved.setPriority(Priority.MEDIUM);
        saved.setDescription(null);
        saved.setStatus(Status.ACTIVE);

        when(taskRepository.save(any())).thenReturn(saved);

        TaskDTO result = taskService.createTask(baseDto);
        assertEquals("Simple title", result.getTitle());
        assertNull(result.getDescription());
        assertEquals(Priority.MEDIUM, result.getPriority());
        assertEquals(Status.ACTIVE, result.getStatus());
    }

    @Test
    @DisplayName("������, ���� ����� �������� ������� �������� ������� ��������")
    void testCreateTask_TitleTooShortAfterMacroRemoval() {
        baseDto.setTitle("!1 ab");
        assertThrows(BadRequestException.class, () -> taskService.createTask(baseDto));
    }


    //func toggleTask Tests
    @Test
    @DisplayName("������������ ������� �� ACTIVE � COMPLETED")
    void toggleTask_ActiveToCompleted() {
        UUID taskId = UUID.randomUUID();
        Task task = new Task();
        task.setId(taskId);
        task.setStatus(Status.ACTIVE);
        task.setUpdatedAt(OffsetDateTime.now().minusDays(1));

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        taskService.toggleTask(taskId);

        assertEquals(Status.COMPLETED, task.getStatus());
        assertTrue(task.getUpdatedAt().isAfter(OffsetDateTime.now().minusSeconds(1)));
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("������������ ������� �� COMPLETED � ACTIVE")
    void toggleTask_CompletedToActive() {
        UUID taskId = UUID.randomUUID();
        Task task = new Task();
        task.setId(taskId);
        task.setStatus(Status.COMPLETED);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        taskService.toggleTask(taskId);

        assertEquals(Status.ACTIVE, task.getStatus());
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("������������ ������� �� OVERDUE � LATE")
    void toggleTask_OverdueToLate() {
        UUID taskId = UUID.randomUUID();
        Task task = new Task();
        task.setId(taskId);
        task.setStatus(Status.OVERDUE);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        taskService.toggleTask(taskId);

        assertEquals(Status.LATE, task.getStatus());
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("������������ ������� �� LATE � OVERDUE")
    void toggleTask_LateToOverdue() {
        UUID taskId = UUID.randomUUID();
        Task task = new Task();
        task.setId(taskId);
        task.setStatus(Status.LATE);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        taskService.toggleTask(taskId);

        assertEquals(Status.OVERDUE, task.getStatus());
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("������� ������������ �������������� ������ �������� NotFoundException")
    void toggleTask_NonExistentTask_ThrowsNotFoundException() {
        UUID taskId = UUID.randomUUID();
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taskService.toggleTask(taskId));
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("���������� ������� updatedAt ��� ������������ �������")
    void toggleTask_UpdatesUpdatedAt() {
        UUID taskId = UUID.randomUUID();
        Task task = new Task();
        task.setId(taskId);
        task.setStatus(Status.ACTIVE);
        OffsetDateTime initialUpdatedAt = OffsetDateTime.now().minusDays(1);
        task.setUpdatedAt(initialUpdatedAt);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        taskService.toggleTask(taskId);

        assertNotEquals(initialUpdatedAt, task.getUpdatedAt());
        assertTrue(task.getUpdatedAt().isAfter(initialUpdatedAt));
        verify(taskRepository).save(task);
    }

    //func updateTask Tests

    @Test
    @DisplayName("�������� ���������� ������ � ������ ����������")
    void testUpdateTask_ValidUpdate() {
        updateTaskDTO.setTitle("New title");
        updateTaskDTO.setDescription("New description");
        updateTaskDTO.setPriority(Priority.HIGH);
        updateTaskDTO.setDeadline(OffsetDateTime.now().plusDays(1));

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        taskService.updateTask(taskId, updateTaskDTO);

        assertEquals("New title", existingTask.getTitle());
        assertEquals("New description", existingTask.getDescription());
        assertEquals(Priority.HIGH, existingTask.getPriority());
        assertEquals(Status.ACTIVE, existingTask.getStatus());
        assertTrue(existingTask.getUpdatedAt().isAfter(OffsetDateTime.now().minusSeconds(1)));
        verify(taskRepository).save(existingTask);
    }

    @Test
    @DisplayName("������ ��� ���������� �������������� ������")
    void testUpdateTask_NonExistentTask_ThrowsNotFoundException() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taskService.updateTask(taskId, updateTaskDTO));
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("������ ��� null UpdateTaskDTO")
    void testUpdateTask_NullDTO_ThrowsBadRequestException() {
        assertThrows(BadRequestException.class, () -> taskService.updateTask(taskId, null));
        verify(taskRepository, never()).findById(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"!1 New critical task", "!2 High priority task", "!3 Medium task", "!4 Low task"})
    @DisplayName("���������� ������ � ��������� ����������")
    void testUpdateTask_PriorityMacro(String title) {
        updateTaskDTO.setTitle(title);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        taskService.updateTask(taskId, updateTaskDTO);

        String expectedTitle = title.replaceFirst("!\\d", "").trim();
        Priority expectedPriority = switch (title.split(" ")[0]) {
            case "!1" -> Priority.CRITICAL;
            case "!2" -> Priority.HIGH;
            case "!3" -> Priority.MEDIUM;
            case "!4" -> Priority.LOW;
            default -> Priority.MEDIUM;
        };

        assertEquals(expectedTitle, existingTask.getTitle());
        assertEquals(expectedPriority, existingTask.getPriority());
        verify(taskRepository).save(existingTask);
    }

    @Test
    @DisplayName("���������� ������ � �������� ��������")
    void testUpdateTask_DeadlineMacro() {
        updateTaskDTO.setTitle("!before 01.01.2030 Updated task");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        taskService.updateTask(taskId, updateTaskDTO);

        assertEquals("Updated task", existingTask.getTitle());
        assertNotNull(existingTask.getDeadline());
        verify(taskRepository).save(existingTask);
    }

    @Test
    @DisplayName("������ ��� �������� ������� �������� ��� ���������� ������")
    void testUpdateTask_InvalidDeadlineMacro_ThrowsException() {
        updateTaskDTO.setTitle("!before 99.99.9999 Invalid date");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        assertThrows(BadRequestException.class, () -> taskService.updateTask(taskId, updateTaskDTO));
    }

    @Test
    @DisplayName("������ ��� �������� � ������������ ������� (13) ��� ����������")
    void testUpdateTask_InvalidMonthInDeadlineMacro_ThrowsException() {
        updateTaskDTO.setTitle("!before 31.13.2025 Invalid month");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        assertThrows(BadRequestException.class, () -> taskService.updateTask(taskId, updateTaskDTO));
    }

    @Test
    @DisplayName("������ ��� �������� � ������������ ������� � �������� ��� ����������")
    void testUpdateTask_InvalidMonthInDeadlineDashMacro_ThrowsException() {
        updateTaskDTO.setTitle("!before 31-13-2025 Invalid month");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        assertThrows(BadRequestException.class, () -> taskService.updateTask(taskId, updateTaskDTO));
    }

    @Test
    @DisplayName("������ ��� �������� � ������������ ���� (32) ��� ����������")
    void testUpdateTask_InvalidDayInDeadlineMacro_ThrowsException() {
        updateTaskDTO.setTitle("!before 32.12.2025 Invalid day");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        assertThrows(BadRequestException.class, () -> taskService.updateTask(taskId, updateTaskDTO));
    }

    @Test
    @DisplayName("������ ��� �������� � ������������ ���� � �������� ��� ����������")
    void testUpdateTask_InvalidDayInDeadlineDashMacro_ThrowsException() {
        updateTaskDTO.setTitle("!before 32-12-2025 Invalid day");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        assertThrows(BadRequestException.class, () -> taskService.updateTask(taskId, updateTaskDTO));
    }

    @Test
    @DisplayName("������ ��� ���� � ������ (00.00.0000) ��� ����������")
    void testUpdateTask_ZeroDateInMacro_ThrowsException() {
        updateTaskDTO.setTitle("!before 00.00.0000 Zero date");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        assertThrows(BadRequestException.class, () -> taskService.updateTask(taskId, updateTaskDTO));
    }

    @Test
    @DisplayName("������ ��� ���� � ������ � �������� (00-00-0000) ��� ����������")
    void testUpdateTask_ZeroDateInMacroDash_ThrowsException() {
        updateTaskDTO.setTitle("!before 00-00-0000 Zero date");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        assertThrows(BadRequestException.class, () -> taskService.updateTask(taskId, updateTaskDTO));
    }

    @Test
    @DisplayName("������ ��� ������� ������ ���� (01.01.0000) ��� ����������")
    void testUpdateTask_TooEarlyDateInMacro_ThrowsException() {
        updateTaskDTO.setTitle("!before 01.01.0000 Too early date");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        assertThrows(BadRequestException.class, () -> taskService.updateTask(taskId, updateTaskDTO));
    }

    @Test
    @DisplayName("������ ��� ������� ������ ���� � �������� (01-01-0000) ��� ����������")
    void testUpdateTask_TooEarlyDateInMacroDash_ThrowsException() {
        updateTaskDTO.setTitle("!before 01-01-0000 Too early date");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        assertThrows(BadRequestException.class, () -> taskService.updateTask(taskId, updateTaskDTO));
    }


    @ParameterizedTest
    @CsvSource({
            "COMPLETED, LATE, true",
            "COMPLETED, COMPLETED, false",
            "ACTIVE, OVERDUE, true",
            "ACTIVE, ACTIVE, false"
    })
    @DisplayName("���������� �������� � �������� �������")
    void testUpdateTask_DeadlineChangeStatus(Status initialStatus, Status expectedStatus, boolean isOverdue) {
        existingTask.setStatus(initialStatus);
        existingTask.setUpdatedAt(OffsetDateTime.now().minusDays(1));
        OffsetDateTime newDeadline = isOverdue ? OffsetDateTime.now().minusDays(1) : OffsetDateTime.now().plusDays(1);
        updateTaskDTO.setDeadline(newDeadline);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        taskService.updateTask(taskId, updateTaskDTO);

        assertEquals(expectedStatus, existingTask.getStatus());
        verify(taskRepository).save(existingTask);
    }

    @Test
    @DisplayName("������ ��� �������� ��������� ����� ��������� ��������")
    void testUpdateTask_ShortTitleAfterMacro_ThrowsBadRequestException() {
        updateTaskDTO.setTitle("!1 ab");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        assertThrows(BadRequestException.class, () -> taskService.updateTask(taskId, updateTaskDTO));
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("������ ��� �������� ������� �������� � �������")
    void testUpdateTask_InvalidDeadlineMacro_ThrowsBadRequestException() {
        updateTaskDTO.setTitle("!before 99.99.9999 Invalid date");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        assertThrows(BadRequestException.class, () -> taskService.updateTask(taskId, updateTaskDTO));
        verify(taskRepository, never()).save(any());
    }

    @ParameterizedTest
    @CsvSource({
            "!1 Critical task, HIGH, HIGH",
            "!2 High task, CRITICAL, CRITICAL",
            "!3 Medium task, null, MEDIUM",
            "!4 Low task, null, LOW"
    })
    @DisplayName("��������� �� DTO ����������� ������")
    void testUpdateTask_PriorityFieldOverridesMacro(String title, String dtoPriority, String expectedPriority) {
        updateTaskDTO.setTitle(title);
        if (dtoPriority != null && !dtoPriority.equals("null")) {
            updateTaskDTO.setPriority(Priority.valueOf(dtoPriority));
        }

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        taskService.updateTask(taskId, updateTaskDTO);

        assertEquals(Priority.valueOf(expectedPriority), existingTask.getPriority());
        verify(taskRepository).save(existingTask);
    }

    @Test
    @DisplayName("���������� ������ �������� � ��������� ���� �� �������� ����� updatedAt")
    void testUpdateTask_OnlyTitleChanged() {
        String originalDescription = existingTask.getDescription();
        Priority originalPriority = existingTask.getPriority();
        OffsetDateTime originalDeadline = existingTask.getDeadline();
        Status originalStatus = existingTask.getStatus();

        updateTaskDTO.setTitle("����� ��������");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        OffsetDateTime beforeUpdate = OffsetDateTime.now().minusSeconds(1);
        taskService.updateTask(taskId, updateTaskDTO);
        OffsetDateTime afterUpdate = OffsetDateTime.now().plusSeconds(1);

        assertEquals("����� ��������", existingTask.getTitle());
        assertEquals(originalDescription, existingTask.getDescription());
        assertEquals(originalPriority, existingTask.getPriority());
        assertEquals(originalDeadline, existingTask.getDeadline());
        assertEquals(originalStatus, existingTask.getStatus());
        assertTrue(existingTask.getUpdatedAt().isAfter(beforeUpdate));
        assertTrue(existingTask.getUpdatedAt().isBefore(afterUpdate));
        verify(taskRepository).save(existingTask);
    }

    @Test
    @DisplayName("���������� ������ ���������� ��� ��������� � ��������� ���� �� �������� ����� updatedAt")
    void testUpdateTask_OnlyPriorityChanged() {
        String originalTitle = existingTask.getTitle();
        String originalDescription = existingTask.getDescription();
        OffsetDateTime originalDeadline = existingTask.getDeadline();
        Status originalStatus = existingTask.getStatus();

        updateTaskDTO.setPriority(Priority.HIGH);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        OffsetDateTime beforeUpdate = OffsetDateTime.now().minusSeconds(1);
        taskService.updateTask(taskId, updateTaskDTO);
        OffsetDateTime afterUpdate = OffsetDateTime.now().plusSeconds(1);

        assertEquals(originalTitle, existingTask.getTitle());
        assertEquals(originalDescription, existingTask.getDescription());
        assertEquals(Priority.HIGH, existingTask.getPriority());
        assertEquals(originalDeadline, existingTask.getDeadline());
        assertEquals(originalStatus, existingTask.getStatus());
        assertTrue(existingTask.getUpdatedAt().isAfter(beforeUpdate));
        assertTrue(existingTask.getUpdatedAt().isBefore(afterUpdate));
        verify(taskRepository).save(existingTask);
    }

    @Test
    @DisplayName("������ � ���������, �� ��������� � ������� ������ ������� � ��� ����� ���������")
    void testUpdateTask_MacroWithManualOverrides() {
        OffsetDateTime manualDeadline = OffsetDateTime.now().plusDays(5);
        updateTaskDTO.setTitle("!1 !before 01.01.2030 Important task");
        updateTaskDTO.setPriority(Priority.LOW);
        updateTaskDTO.setDeadline(manualDeadline);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        taskService.updateTask(taskId, updateTaskDTO);

        assertEquals("Important task", existingTask.getTitle());
        assertEquals(Priority.LOW, existingTask.getPriority());
        assertEquals(manualDeadline, existingTask.getDeadline());
        verify(taskRepository).save(existingTask);
    }

    @Test
    @DisplayName("�������� �������� (��������� null) � ������ ������������ �� ACTIVE")
    void testUpdateTask_DeleteDeadline_ResetsStatus() {
        existingTask.setDeadline(OffsetDateTime.now().plusDays(1));
        existingTask.setStatus(Status.OVERDUE);

        updateTaskDTO.setDeadline(null);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        taskService.updateTask(taskId, updateTaskDTO);

        assertNull(existingTask.getDeadline());
        assertEquals(Status.ACTIVE, existingTask.getStatus());
        verify(taskRepository).save(existingTask);
    }

    @Test
    @DisplayName("���������� ��������: ������� � ��������� � ��� �����������")
    void testUpdateTask_CombinedMacros_AppliedCorrectly() {
        updateTaskDTO.setTitle("!2 !before 01.01.2031 ������ � ���������");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        taskService.updateTask(taskId, updateTaskDTO);

        assertEquals("������ � ���������", existingTask.getTitle());
        assertEquals(Priority.HIGH, existingTask.getPriority());
        assertNotNull(existingTask.getDeadline());
        assertEquals(Status.ACTIVE, existingTask.getStatus());
        verify(taskRepository).save(existingTask);
    }

    @Test
    @DisplayName("������ ���������� ��� description � ������ �������� �����������")
    void testUpdateTask_PriorityMacroOnly_DescriptionUnchanged() {
        String originalDescription = existingTask.getDescription();
        updateTaskDTO.setTitle("!3 ������ ���������");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        taskService.updateTask(taskId, updateTaskDTO);

        assertEquals("������ ���������", existingTask.getTitle());
        assertEquals(originalDescription, existingTask.getDescription());
        assertEquals(Priority.MEDIUM, existingTask.getPriority());
        verify(taskRepository).save(existingTask);
    }

    @Test
    @DisplayName("��������� ���������� � null � ��������� ������������")
    void testUpdateTask_ClearPriority() {
        existingTask.setPriority(Priority.CRITICAL);
        updateTaskDTO.setPriority(null);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        taskService.updateTask(taskId, updateTaskDTO);

        assertNull(existingTask.getPriority());
        verify(taskRepository).save(existingTask);
    }

    @Test
    @DisplayName("��������� �������� � ������� � ������ �������� �� OVERDUE")
    void testUpdateTask_DeadlineInPast_ChangesStatusToOverdue() {
        updateTaskDTO.setDeadline(OffsetDateTime.now().minusDays(2));

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        taskService.updateTask(taskId, updateTaskDTO);

        assertEquals(Status.OVERDUE, existingTask.getStatus());
        verify(taskRepository).save(existingTask);
    }

    @Test
    @DisplayName("��������� �������� � ������� � ������ ACTIVE")
    void testUpdateTask_DeadlineInFuture_StatusActive() {
        updateTaskDTO.setDeadline(OffsetDateTime.now().plusDays(3));
        existingTask.setStatus(Status.OVERDUE);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        taskService.updateTask(taskId, updateTaskDTO);

        assertEquals(Status.ACTIVE, existingTask.getStatus());
        verify(taskRepository).save(existingTask);
    }

    @Test
    @DisplayName("������ ��� ������������ ������� ���� � ������� �������� � ������������� ����������")
    void testUpdateTask_InvalidDeadlineMacroFormat_ThrowsException() {
        updateTaskDTO.setTitle("!before 99-99-9999 ������������ ����");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        assertThrows(BadRequestException.class, () -> taskService.updateTask(taskId, updateTaskDTO));
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("���������� ������ � ��������� � ������� � ������ �������� �� OVERDUE")
    void testUpdateTask_DeadlinePast_StatusOverdue() {
        existingTask.setDeadline(OffsetDateTime.now().minusDays(1));
        existingTask.setStatus(Status.ACTIVE);

        updateTaskDTO.setDeadline(OffsetDateTime.now().minusDays(1));

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        taskService.updateTask(taskId, updateTaskDTO);

        assertEquals(Status.OVERDUE, existingTask.getStatus());
        verify(taskRepository).save(existingTask);
    }

    @Test
    @DisplayName("���������� ������ � ���������� ������ �������� � ����������� ���� ��������� �����")
    void testUpdateTask_OnlyDescriptionChanged_FieldsNotChanged() {
        String newDescription = "����� �������� ������";

        updateTaskDTO.setDescription(newDescription);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        taskService.updateTask(taskId, updateTaskDTO);

        assertEquals(newDescription, existingTask.getDescription());
        assertEquals(existingTask.getTitle(), existingTask.getTitle());
        assertEquals(existingTask.getPriority(), existingTask.getPriority());
        assertEquals(existingTask.getDeadline(), existingTask.getDeadline());
        verify(taskRepository).save(existingTask);
    }

    @Test
    @DisplayName("���������� ������ � ���������� ���������� � ����������� �������� ��������")
    void testUpdateTask_PriorityChanged_DeadlineNotChanged() {
        updateTaskDTO.setPriority(Priority.HIGH);
        updateTaskDTO.setDeadline(existingTask.getDeadline());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        taskService.updateTask(taskId, updateTaskDTO);

        assertEquals(Priority.HIGH, existingTask.getPriority());
        assertEquals(existingTask.getDeadline(), existingTask.getDeadline());
        verify(taskRepository).save(existingTask);
    }

    @Test
    @DisplayName("���������� ������ � ���������� ������� � ���� ������ �����")
    void testUpdateTask_AllFieldsChanged() {
        OffsetDateTime expectedDeadline = OffsetDateTime.now().plusDays(3).truncatedTo(ChronoUnit.SECONDS);
        updateTaskDTO.setTitle("����� ���������");
        updateTaskDTO.setDescription("����� ��������");
        updateTaskDTO.setPriority(Priority.CRITICAL);
        updateTaskDTO.setDeadline(expectedDeadline);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        taskService.updateTask(taskId, updateTaskDTO);

        assertEquals("����� ���������", existingTask.getTitle());
        assertEquals("����� ��������", existingTask.getDescription());
        assertEquals(Priority.CRITICAL, existingTask.getPriority());
        assertEquals(expectedDeadline, existingTask.getDeadline());
        verify(taskRepository).save(existingTask);
    }

    @Test
    @DisplayName("�������� ������������ ������")
    void testDeleteTask_ExistingTask() {
        UUID taskId = UUID.randomUUID();
        Task existingTask = new Task();
        existingTask.setId(taskId);

        when(taskRepository.existsById(taskId)).thenReturn(true);
        doNothing().when(taskRepository).deleteById(taskId);

        taskService.deleteTask(taskId);

        verify(taskRepository).deleteById(taskId);
    }

    @Test
    @DisplayName("������ ��� �������� �������������� ������")
    void testDeleteTask_NonExistentTask_ThrowsNotFoundException() {
        UUID taskId = UUID.randomUUID();

        when(taskRepository.existsById(taskId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> taskService.deleteTask(taskId));
        verify(taskRepository, never()).deleteById(taskId);
    }



}

