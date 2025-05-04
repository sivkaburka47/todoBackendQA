package ru.hits.todobackend.Specification;

import org.springframework.data.jpa.domain.Specification;
import ru.hits.todobackend.entities.Task;
import ru.hits.todobackend.entities.enum_entities.Priority;
import ru.hits.todobackend.entities.enum_entities.Status;

import java.time.OffsetDateTime;

import org.springframework.data.jpa.domain.Specification;
import java.time.OffsetDateTime;

public class TaskSpecifications {

    public static Specification<Task> hasStatus(Status status) {
        return (root, query, cb) ->
                status != null ? cb.equal(root.get("status"), status) : null;
    }

    public static Specification<Task> hasPriority(Priority priority) {
        return (root, query, cb) ->
                priority != null ? cb.equal(root.get("priority"), priority) : null;
    }

    public static Specification<Task> deadlineBetween(
            OffsetDateTime from,
            OffsetDateTime to
    ) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from != null && to != null)
                return cb.between(root.get("deadline"), from, to);
            if (from != null)
                return cb.greaterThanOrEqualTo(root.get("deadline"), from);
            return cb.lessThanOrEqualTo(root.get("deadline"), to);
        };
    }

    public static Specification<Task> deadlineBefore(OffsetDateTime deadline) {
        return (root, query, cb) -> deadline == null ? null :
                cb.and(
                        cb.isNotNull(root.get("deadline")),
                        cb.lessThan(root.get("deadline"), deadline)
                );
    }
}