package ru.hits.todobackend.dto;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ErrorResponse {
    private String status;
    private String message;

    public ErrorResponse(HttpStatus httpStatus, String message) {
        this.status = httpStatus.getReasonPhrase();
        this.message = message;
    }
}