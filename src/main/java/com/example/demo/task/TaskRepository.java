package com.example.demo.task;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.example.demo.user.User;
import com.example.demo.userstory.UserStoryStatus;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStoryId(Long storyId);
    List<Task> findByAssignees_Id(Long userId);
    List<Task> findByAssignees(User user);
    List<Task> findByStory_Status(UserStoryStatus status);
    List<Task> findByStatusNotAndEstimateHoursIsNotNull(TaskStatus status);
}