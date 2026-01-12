package com.example.demo.task;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.example.demo.user.User;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStoryId(Long storyId);
    List<Task> findByAssignedToId(Long userId);
    List<Task> findByAssignedTo(User user);
}
