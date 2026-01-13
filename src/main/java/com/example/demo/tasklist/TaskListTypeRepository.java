package com.example.demo.tasklist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskListTypeRepository extends JpaRepository<TaskListType, Long> {
    Optional<TaskListType> findByCode(String code);
}