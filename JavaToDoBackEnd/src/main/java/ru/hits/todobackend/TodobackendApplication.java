package ru.hits.todobackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TodobackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodobackendApplication.class, args);
    }

}
