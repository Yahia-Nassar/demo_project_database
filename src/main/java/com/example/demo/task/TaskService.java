package com.example.demo.task;

import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import com.example.demo.userstory.UserStory;
import com.example.demo.userstory.UserStoryStatus;
import com.example.demo.userstory.UserStoryRepository;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class TaskService {

    private final TaskRepository taskRepo;
    private final UserStoryRepository storyRepo;
    private final UserRepository userRepo;

    public TaskService(
            TaskRepository taskRepo,
            UserStoryRepository storyRepo,
            UserRepository userRepo
    ) {
        this.taskRepo = taskRepo;
        this.storyRepo = storyRepo;
        this.userRepo = userRepo;
    }

    public Task create(Long storyId, String title) {
        UserStory story = storyRepo.findById(storyId).orElseThrow();

        Task task = new Task();
        task.setTitle(title);
        task.setStatus(TaskStatus.TODO);
        task.setStory(story);

        return taskRepo.save(task);
    }

    public void assign(Long taskId, Long userId) {
        assign(taskId, List.of(userId));
    }

    public void assign(Long taskId, List<Long> userIds) {
        Task task = taskRepo.findById(taskId).orElseThrow();
        List<User> users = userRepo.findAllById(userIds);
        task.getAssignees().clear();
        task.getAssignees().addAll(users);
        taskRepo.save(task);
    }

    public Task findById(Long taskId) {
        return taskRepo.findById(taskId).orElseThrow();
    }

    public Task update(Long taskId, String title, List<Long> assigneeIds) {
        Task task = taskRepo.findById(taskId).orElseThrow();
        task.setTitle(title);
        if (assigneeIds != null) {
            List<User> users = userRepo.findAllById(assigneeIds);
            task.getAssignees().clear();
            task.getAssignees().addAll(users);
        }
        return taskRepo.save(task);
    }

    public void markDone(Long taskId) {
        changeStatus(taskId, TaskStatus.DONE);
    }

    public void changeStatus(Long taskId, TaskStatus targetStatus) {
        Task task = taskRepo.findById(taskId).orElseThrow();
        TaskStatus current = task.getStatus();
        if (!allowedTransitions(current).contains(targetStatus)) {
            throw new IllegalStateException(
                    "Invalid status transition from " + current + " to " + targetStatus
            );
        }
        task.setStatus(targetStatus);
        taskRepo.save(task);
    }

    public Set<TaskStatus> allowedTransitions(TaskStatus currentStatus) {
        if (currentStatus == null) {
            return EnumSet.of(TaskStatus.TODO);
        }
        return switch (currentStatus) {
            case TODO -> EnumSet.of(TaskStatus.IN_PROGRESS, TaskStatus.BLOCKED);
            case IN_PROGRESS -> EnumSet.of(TaskStatus.REVIEW, TaskStatus.BLOCKED, TaskStatus.TODO);
            case REVIEW -> EnumSet.of(TaskStatus.TEST, TaskStatus.BLOCKED, TaskStatus.IN_PROGRESS);
            case TEST -> EnumSet.of(TaskStatus.DONE, TaskStatus.BLOCKED, TaskStatus.IN_PROGRESS);
            case BLOCKED -> EnumSet.of(TaskStatus.IN_PROGRESS, TaskStatus.TODO);
            case DONE -> EnumSet.of(TaskStatus.IN_PROGRESS);
        };
    }


    public List<Task> forStory(Long storyId) {
        return taskRepo.findByStoryId(storyId);
    }

    public List<Task> forUser(Long userId) {
        return taskRepo.findByAssignees_Id(userId);
    }

    public List<Task> findByDeveloper(User developer) {
        return taskRepo.findByAssignees(developer);
    }

    public List<Task> sprintTasks() {
        return taskRepo.findByStory_Status(UserStoryStatus.SPRINT);
    }
}
