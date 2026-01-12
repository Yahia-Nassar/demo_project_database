package com.example.demo.task;

import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import com.example.demo.userstory.UserStory;
import com.example.demo.userstory.UserStoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
        Task task = taskRepo.findById(taskId).orElseThrow();
        User user = userRepo.findById(userId).orElseThrow();
        task.setAssignedTo(user);
        taskRepo.save(task);
    }

    public void markDone(Long taskId) {
        Task task = taskRepo.findById(taskId).orElseThrow();
        task.setStatus(TaskStatus.DONE);
        taskRepo.save(task);
    }

    public List<Task> forStory(Long storyId) {
        return taskRepo.findByStoryId(storyId);
    }

    public List<Task> forUser(Long userId) {
        return taskRepo.findByAssignedToId(userId);
    }
}
