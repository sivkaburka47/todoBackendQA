package ru.hits.todobackend.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.hits.todobackend.Specification.TaskSpecifications;
import ru.hits.todobackend.entities.Task;
import ru.hits.todobackend.entities.enum_entities.Status;
import ru.hits.todobackend.repository.TaskRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeadlineCheckService {

    private static final Logger logger = LoggerFactory.getLogger(DeadlineCheckService.class);
    private final TaskRepository taskRepository;

    @Scheduled(fixedRate = 300000)
    public void checkOverdueTasks() {
        OffsetDateTime now = OffsetDateTime.now();
        Specification<Task> spec = Specification.where(TaskSpecifications.hasStatus(Status.ACTIVE))
                .and(TaskSpecifications.deadlineBefore(now));

        List<Task> overdueTasks = taskRepository.findAll(spec);
        for (Task task : overdueTasks) {
            task.setStatus(Status.OVERDUE);
            task.setUpdatedAt(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC));
        }

        if (!overdueTasks.isEmpty()) {
            taskRepository.saveAll(overdueTasks);
            logger.info("!!!!! !!!!! Updated {} tasks to OVERDUE status", overdueTasks.size());
        } else {
            logger.debug("!!!!! !!!!! No tasks found with overdue deadlines");
        }
    }
}
