package com.example.demo.task;

import com.example.demo.notification.NotificationService;
import com.example.demo.notification.NotificationType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskReminderService {

    private static final Duration DEFAULT_REMINDER_WINDOW = Duration.ofHours(4);

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    public TaskReminderService(TaskRepository taskRepository, NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedDelayString = "${task.reminder.interval-ms:3600000}")
    @Transactional
    public void sendDueSoonReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plus(DEFAULT_REMINDER_WINDOW);
        List<Task> candidates = taskRepository.findWithAssigneesForReminder(TaskStatus.DONE);
        for (Task task : candidates) {
            LocalDateTime anchorTime = task.getStartedAt() != null
                    ? task.getStartedAt()
                    : task.getCreatedAt();
            if (anchorTime == null || task.getEstimateHours() == null) {
                continue;
            }
            if (task.getReminderSentAt() != null) {
                continue;
            }
            LocalDateTime dueAt = anchorTime
                    .plusMinutes(Math.round(task.getEstimateHours() * 60));
            if (dueAt.isAfter(now) && !dueAt.isAfter(windowEnd) && !task.getAssignees().isEmpty()) {
                String message = "Task \"" + task.getTitle()
                        + "\" should be finished soon (estimate ends at " + dueAt + ").";
                notificationService.notifyUsers(task.getAssignees(), message, NotificationType.TASK_DUE_SOON);
                task.setReminderSentAt(LocalDateTime.now());
                taskRepository.save(task);
            }
        }
    }
}