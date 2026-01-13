package com.example.demo.tasklist;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskListTypeService {

    private final TaskListTypeRepository repository;

    public TaskListTypeService(TaskListTypeRepository repository) {
        this.repository = repository;
    }

    public List<TaskListType> all() {
        return repository.findAll();
    }

    public void ensureDefaults() {
        seedIfMissing("TODO", "To Do", 1);
        seedIfMissing("IN_PROGRESS", "In Progress", 2);
        seedIfMissing("REVIEW", "Review", 3);
        seedIfMissing("TEST", "Test", 4);
        seedIfMissing("BLOCKED", "Blocked", 5);
        seedIfMissing("DONE", "Done", 6);
    }

    private void seedIfMissing(String code, String label, int order) {
        repository.findByCode(code).orElseGet(() -> {
            TaskListType type = new TaskListType();
            type.setCode(code);
            type.setLabel(label);
            type.setDisplayOrder(order);
            return repository.save(type);
        });
    }
}