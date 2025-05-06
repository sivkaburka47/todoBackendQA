package ru.hits.todobackend.controllersTests;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import ru.hits.todobackend.dto.CreateTaskDTO;
import ru.hits.todobackend.dto.TaskDTO;
import ru.hits.todobackend.dto.UpdateTaskDTO;
import ru.hits.todobackend.entities.enum_entities.Priority;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TaskControllerTests {

    @LocalServerPort
    private int port;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/tasks";
    }

    @Test
    @DisplayName("Создание задачи с валидными данными должно возвращать 201 CREATED")
    void createTask_WithValidData_ShouldReturn201Created() {
        CreateTaskDTO createTaskDTO = new CreateTaskDTO();
        createTaskDTO.setTitle("Тестовая задача");
        createTaskDTO.setDescription("Описание тестовой задачи");

        given()
                .contentType(ContentType.JSON)
                .body(createTaskDTO)
                .when()
                .post(getBaseUrl())
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("title", equalTo("Тестовая задача"))
                .body("description", equalTo("Описание тестовой задачи"));
    }

    @Test
    @DisplayName("Создание задачи без описания должно быть успешным")
    void createTask_WithoutDescription_ShouldBeSuccessful() {
        CreateTaskDTO createTaskDTO = new CreateTaskDTO();
        createTaskDTO.setTitle("Задача без описания");

        given()
                .contentType(ContentType.JSON)
                .body(createTaskDTO)
                .when()
                .post(getBaseUrl())
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("description", nullValue());
    }

    @Test
    @DisplayName("Создание задачи с приоритетом должно сохранять приоритет")
    void createTask_WithPriority_ShouldSavePriority() {
        CreateTaskDTO createTaskDTO = new CreateTaskDTO();
        createTaskDTO.setTitle("Важная задача");
        createTaskDTO.setPriority(Priority.HIGH);

        given()
                .contentType(ContentType.JSON)
                .body(createTaskDTO)
                .when()
                .post(getBaseUrl())
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("priority", equalTo("HIGH"));
    }

    @Test
    @DisplayName("Создание задачи с дедлайном должно сохранять дедлайн")
    void createTask_WithDeadline_ShouldSaveDeadline() {
        CreateTaskDTO createTaskDTO = new CreateTaskDTO();
        createTaskDTO.setTitle("Задача с дедлайном");
        createTaskDTO.setDeadline(OffsetDateTime.now().plusDays(1));

        given()
                .contentType(ContentType.JSON)
                .body(createTaskDTO)
                .when()
                .post(getBaseUrl())
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("deadline", notNullValue());
    }

    @Test
    @DisplayName("Созданная задача должна иметь статус ACTIVE по умолчанию")
    void createTask_ShouldHaveActiveStatusByDefault() {
        CreateTaskDTO createTaskDTO = new CreateTaskDTO();
        createTaskDTO.setTitle("Задача со статусом по умолчанию");

        given()
                .contentType(ContentType.JSON)
                .body(createTaskDTO)
                .when()
                .post(getBaseUrl())
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("status", equalTo("ACTIVE"));
    }

    @Test
    @DisplayName("Созданная задача должна иметь дату создания и дату обновления")
    void createTask_ShouldHaveCreatedAtDate() {
        CreateTaskDTO createTaskDTO = new CreateTaskDTO();
        createTaskDTO.setTitle("Задача с датой создания");

        given()
                .contentType(ContentType.JSON)
                .body(createTaskDTO)
                .when()
                .post(getBaseUrl())
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue());
    }

    @Test
    @DisplayName("Две созданные задачи должны иметь разные ID")
    void createTwoTasks_ShouldHaveDifferentIds() {
        CreateTaskDTO task1 = new CreateTaskDTO();
        task1.setTitle("Первая задача");

        CreateTaskDTO task2 = new CreateTaskDTO();
        task2.setTitle("Вторая задача");

        UUID id1 = given()
                .contentType(ContentType.JSON)
                .body(task1)
                .when()
                .post(getBaseUrl())
                .then()
                .extract().as(TaskDTO.class).getId();

        UUID id2 = given()
                .contentType(ContentType.JSON)
                .body(task2)
                .when()
                .post(getBaseUrl())
                .then()
                .extract().as(TaskDTO.class).getId();

        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("Создание задачи без заголовка должно возвращать 400 BAD_REQUEST")
    void createTask_WithoutTitle_ShouldReturn400() {
        CreateTaskDTO createTaskDTO = new CreateTaskDTO();
        createTaskDTO.setDescription("Описание без заголовка");

        given()
                .contentType(ContentType.JSON)
                .body(createTaskDTO)
                .when()
                .post(getBaseUrl())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Создание задачи с пустым заголовком должно возвращать 400 BAD_REQUEST")
    void createTask_WithEmptyTitle_ShouldReturn400() {
        CreateTaskDTO createTaskDTO = new CreateTaskDTO();
        createTaskDTO.setTitle("");
        createTaskDTO.setDescription("Описание с пустым заголовком");

        given()
                .contentType(ContentType.JSON)
                .body(createTaskDTO)
                .when()
                .post(getBaseUrl())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Обновление задачи с валидными данными должно возвращать 204 NO_CONTENT")
    void updateTask_WithValidData_ShouldReturn204() {
        CreateTaskDTO createTaskDTO = new CreateTaskDTO();
        createTaskDTO.setTitle("Оригинальный заголовок");

        TaskDTO createdTask = given()
                .contentType(ContentType.JSON)
                .body(createTaskDTO)
                .when()
                .post(getBaseUrl())
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().as(TaskDTO.class);

        UpdateTaskDTO updateTaskDTO = new UpdateTaskDTO();
        updateTaskDTO.setTitle("Обновлённый заголовок");
        updateTaskDTO.setDescription("Новое описание");

        given()
                .contentType(ContentType.JSON)
                .body(updateTaskDTO)
                .when()
                .put(getBaseUrl() + "/" + createdTask.getId() + "/update")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        given()
                .when()
                .get(getBaseUrl() + "/" + createdTask.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("title", equalTo("Обновлённый заголовок"))
                .body("description", equalTo("Новое описание"));
    }

    @Test
    @DisplayName("Обновление задачи с пустым заголовком должно возвращать 400 BAD_REQUEST")
    void updateTask_WithEmptyTitle_ShouldReturn400() {
        CreateTaskDTO createTaskDTO = new CreateTaskDTO();
        createTaskDTO.setTitle("Заголовок");

        TaskDTO createdTask = given()
                .contentType(ContentType.JSON)
                .body(createTaskDTO)
                .when()
                .post(getBaseUrl())
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().as(TaskDTO.class);

        UpdateTaskDTO updateTaskDTO = new UpdateTaskDTO();
        updateTaskDTO.setTitle("");

        given()
                .contentType(ContentType.JSON)
                .body(updateTaskDTO)
                .when()
                .put(getBaseUrl() + "/" + createdTask.getId() + "/update")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Обновление несуществующей задачи должно возвращать 404 NOT_FOUND")
    void updateNonExistentTask_ShouldReturn404() {
        UUID fakeId = UUID.randomUUID();

        UpdateTaskDTO updateTaskDTO = new UpdateTaskDTO();
        updateTaskDTO.setTitle("Обновление несуществующей");

        given()
                .contentType(ContentType.JSON)
                .body(updateTaskDTO)
                .when()
                .put(getBaseUrl() + "/" + fakeId + "/update")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Обновление задачи с невалидным значением приоритета должно возвращать 400 BAD_REQUEST")
    void updateTask_InvalidPriority_ShouldReturn400() {
        CreateTaskDTO createTaskDTO = new CreateTaskDTO();
        createTaskDTO.setTitle("Задача для проверки приоритета");

        TaskDTO createdTask = given()
                .contentType(ContentType.JSON)
                .body(createTaskDTO)
                .post(getBaseUrl())
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().as(TaskDTO.class);

        String invalidJson = """
        {
            "priority": "EXTREME"
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .put(getBaseUrl() + "/" + createdTask.getId() + "/update")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Обновление задачи с некорректным форматом даты должно возвращать 400 BAD_REQUEST")
    void updateTask_InvalidDateFormat_ShouldReturn400() {
        CreateTaskDTO createTaskDTO = new CreateTaskDTO();
        createTaskDTO.setTitle("Задача с неправильной датой");

        TaskDTO createdTask = given()
                .contentType(ContentType.JSON)
                .body(createTaskDTO)
                .post(getBaseUrl())
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().as(TaskDTO.class);

        String invalidJson = """
        {
            "deadline": "2024-13-45T99:99:99Z"
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .put(getBaseUrl() + "/" + createdTask.getId() + "/update")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Обновление задачи с невалидным JSON должно возвращать 400 BAD_REQUEST")
    void updateTask_InvalidJson_ShouldReturn400() {
        CreateTaskDTO createTaskDTO = new CreateTaskDTO();
        createTaskDTO.setTitle("Задача для невалидного JSON");

        TaskDTO createdTask = given()
                .contentType(ContentType.JSON)
                .body(createTaskDTO)
                .post(getBaseUrl())
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().as(TaskDTO.class);

        String malformedJson = "{ \"priority\": HIGH ";

        given()
                .contentType(ContentType.JSON)
                .body(malformedJson)
                .put(getBaseUrl() + "/" + createdTask.getId() + "/update")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }




    @Test
    @DisplayName("Обновление только приоритета задачи должно быть успешным")
    void updateTask_OnlyPriority_ShouldBeSuccessful() {
        CreateTaskDTO createTaskDTO = new CreateTaskDTO();
        createTaskDTO.setTitle("Задача для изменения приоритета");

        TaskDTO createdTask = given()
                .contentType(ContentType.JSON)
                .body(createTaskDTO)
                .when()
                .post(getBaseUrl())
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().as(TaskDTO.class);

        UpdateTaskDTO updateTaskDTO = new UpdateTaskDTO();
        updateTaskDTO.setPriority(Priority.CRITICAL);

        given()
                .contentType(ContentType.JSON)
                .body(updateTaskDTO)
                .when()
                .put(getBaseUrl() + "/" + createdTask.getId() + "/update")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        given()
                .when()
                .get(getBaseUrl() + "/" + createdTask.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("priority", equalTo("CRITICAL"));
    }


    @Test
    @DisplayName("Переключение статуса задачи должно быть успешным")
    void toggleTaskStatus_ShouldToggleSuccessfully() {
        CreateTaskDTO createTaskDTO = new CreateTaskDTO();
        createTaskDTO.setTitle("Задача для переключения статуса");

        TaskDTO createdTask = given()
                .contentType(ContentType.JSON)
                .body(createTaskDTO)
                .when()
                .post(getBaseUrl())
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().as(TaskDTO.class);

        assertEquals("ACTIVE", createdTask.getStatus().name());

        given()
                .when()
                .patch(getBaseUrl() + "/" + createdTask.getId() + "/toggle")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        given()
                .when()
                .get(getBaseUrl() + "/" + createdTask.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("status", equalTo("COMPLETED"));

        given()
                .when()
                .patch(getBaseUrl() + "/" + createdTask.getId() + "/toggle")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        given()
                .when()
                .get(getBaseUrl() + "/" + createdTask.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("status", equalTo("ACTIVE"));
    }

    @Test
    @DisplayName("Попытка переключить статус несуществующей задачи должна вернуть 404 NOT_FOUND")
    void toggleTaskStatus_NonExistentTask_ShouldReturn404() {
        UUID fakeId = UUID.randomUUID();

        given()
                .when()
                .patch(getBaseUrl() + "/" + fakeId + "/toggle")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Удаление существующей задачи должно быть успешным")
    void deleteTask_ShouldDeleteSuccessfully() {
        CreateTaskDTO createTaskDTO = new CreateTaskDTO();
        createTaskDTO.setTitle("Задача для удаления");

        TaskDTO createdTask = given()
                .contentType(ContentType.JSON)
                .body(createTaskDTO)
                .when()
                .post(getBaseUrl())
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().as(TaskDTO.class);

        given()
                .when()
                .delete(getBaseUrl() + "/" + createdTask.getId() + "/delete")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        given()
                .when()
                .get(getBaseUrl() + "/" + createdTask.getId())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Удаление несуществующей задачи должно вернуть 404 NOT_FOUND")
    void deleteTask_NonExistent_ShouldReturn404() {
        UUID fakeId = UUID.randomUUID();

        given()
                .when()
                .delete(getBaseUrl() + "/" + fakeId + "/delete")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }


    @Test
    @DisplayName("Получение задачи по ID должно быть успешным")
    void getTaskById_ShouldReturnTask() {
        CreateTaskDTO createTaskDTO = new CreateTaskDTO();
        createTaskDTO.setTitle("Задача для получения по ID");

        TaskDTO createdTask = given()
                .contentType(ContentType.JSON)
                .body(createTaskDTO)
                .when()
                .post(getBaseUrl())
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().as(TaskDTO.class);

        given()
                .when()
                .get(getBaseUrl() + "/" + createdTask.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(createdTask.getId().toString()))
                .body("title", equalTo("Задача для получения по ID"));
    }

    @Test
    @DisplayName("Получение задачи по несуществующему ID должно вернуть 404 NOT_FOUND")
    void getTaskById_NonExistent_ShouldReturn404() {
        UUID fakeId = UUID.randomUUID();

        given()
                .when()
                .get(getBaseUrl() + "/" + fakeId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Получение всех задач без фильтров должно вернуть список задач")
    void getAllTasks_WithoutFilters_ShouldReturnTasksList() {
        CreateTaskDTO createTaskDTO = new CreateTaskDTO();
        createTaskDTO.setTitle("Общая задача");

        given()
                .contentType(ContentType.JSON)
                .body(createTaskDTO)
                .when()
                .post(getBaseUrl())
                .then()
                .statusCode(HttpStatus.CREATED.value());

        given()
                .when()
                .get(getBaseUrl())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", greaterThan(0));
    }

    @Test
    @DisplayName("Фильтрация задач по статусу должна работать корректно")
    void getAllTasks_FilterByStatus_ShouldWork() {
        CreateTaskDTO taskDTO = new CreateTaskDTO();
        taskDTO.setTitle("Фильтрация по статусу");

        TaskDTO createdTask = given()
                .contentType(ContentType.JSON)
                .body(taskDTO)
                .when()
                .post(getBaseUrl())
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().as(TaskDTO.class);

        given()
                .queryParam("status", "ACTIVE")
                .when()
                .get(getBaseUrl())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("findAll { it.status == 'ACTIVE' }.size()", greaterThan(0));
    }

    @Test
    @DisplayName("Фильтрация задач по приоритету должна работать корректно")
    void getAllTasks_FilterByPriority_ShouldReturnOnlyMatchingPriority() {
        CreateTaskDTO dto = new CreateTaskDTO();
        dto.setTitle("Критическая задача");

        TaskDTO task = given().contentType(ContentType.JSON).body(dto).post(getBaseUrl()).then().extract().as(TaskDTO.class);

        UpdateTaskDTO updateDTO = new UpdateTaskDTO();
        updateDTO.setPriority(Priority.CRITICAL);

        given().contentType(ContentType.JSON)
                .body(updateDTO)
                .put(getBaseUrl() + "/" + task.getId() + "/update")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        given()
                .queryParam("priority", "CRITICAL")
                .when()
                .get(getBaseUrl())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("findAll { it.priority == 'CRITICAL' }.size()", greaterThanOrEqualTo(1));
    }

    @Test
    @DisplayName("Фильтрация задач по диапазону сроков должна работать корректно")
    void getAllTasks_FilterByDeadlineRange_ShouldReturnMatchingTasks() {
        CreateTaskDTO dto = new CreateTaskDTO();
        dto.setTitle("Задача с дедлайном");
        dto.setDeadline(OffsetDateTime.now().plusDays(5));

        given().contentType(ContentType.JSON).body(dto).post(getBaseUrl());

        given()
                .queryParam("deadlineFrom", OffsetDateTime.now().plusDays(1).toString())
                .queryParam("deadlineTo", OffsetDateTime.now().plusDays(10).toString())
                .when()
                .get(getBaseUrl())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", greaterThanOrEqualTo(1));
    }



    @Test
    @DisplayName("Сортировка задач по дате создания по убыванию должна корректно упорядочивать список")
    void getAllTasks_SortByCreatedAtDesc_ShouldBeInDescendingOrder() {
        CreateTaskDTO taskDTO1 = new CreateTaskDTO();
        taskDTO1.setTitle("Первая задача");
        given().contentType(ContentType.JSON).body(taskDTO1).post(getBaseUrl());

        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        CreateTaskDTO taskDTO2 = new CreateTaskDTO();
        taskDTO2.setTitle("Вторая задача");
        given().contentType(ContentType.JSON).body(taskDTO2).post(getBaseUrl());

        List<Map<String, Object>> tasks = given()
                .queryParam("sortBy", "CREATED_AT")
                .queryParam("direction", "DESC")
                .when()
                .get(getBaseUrl())
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList("");

        List<OffsetDateTime> createdAts = tasks.stream()
                .map(task -> OffsetDateTime.parse((String) task.get("createdAt")))
                .collect(Collectors.toList());

        for (int i = 0; i < createdAts.size() - 1; i++) {
            assertTrue(
                    createdAts.get(i).isAfter(createdAts.get(i + 1)) || createdAts.get(i).isEqual(createdAts.get(i + 1)),
                    "Задачи не отсортированы по createdAt по убыванию"
            );
        }
    }



}