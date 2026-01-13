package com.example.demo.tasklist;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskListTypeConfig {

    @Bean
    public CommandLineRunner taskListTypeSeeder(TaskListTypeService service) {
        return args -> service.ensureDefaults();
    }
}