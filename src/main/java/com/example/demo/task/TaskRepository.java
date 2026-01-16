package com.example.demo.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import com.example.demo.user.User;
import com.example.demo.userstory.UserStoryStatus;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStoryId(Long storyId);
    List<Task> findByAssignees_Id(Long userId);
    List<Task> findByAssignees(User user);
    List<Task> findByStory_Status(UserStoryStatus status);
    @Query("select distinct t from Task t left join fetch t.assignees "
            + "where t.status <> :status and t.estimateHours is not null")
    List<Task> findWithAssigneesForReminder(@Param("status") TaskStatus status);
}