package com.example.demo.task;

import com.example.demo.notification.NotificationService;
import com.example.demo.notification.NotificationType;
import com.example.demo.security.Role;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TaskReminderService {

    private static final int DEFAULT_REMINDER_LEAD_MINUTES = 60;

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final int reminderLeadMinutes;

    public TaskReminderService(TaskRepository taskRepository,
                               NotificationService notificationService,
                               UserRepository userRepository,
                               @org.springframework.beans.factory.annotation.Value(
                                       "${task.reminder.lead-minutes:" + DEFAULT_REMINDER_LEAD_MINUTES + "}"
                               ) int reminderLeadMinutes) {
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.reminderLeadMinutes = reminderLeadMinutes;
    }

    @Scheduled(fixedDelayString = "${task.reminder.interval-ms:60000}")
    @Transactional
    public void sendDueSoonReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<Task> candidates = taskRepository.findWithAssigneesForReminder(TaskStatus.DONE);
        List<User> productOwners = userRepository.findByRole(Role.PO);
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
            int leadMinutes = task.getReminderLeadMinutes() != null
                    ? task.getReminderLeadMinutes()
                    : reminderLeadMinutes;
            LocalDateTime reminderAt = dueAt.minusMinutes(leadMinutes);
            if (!reminderAt.isAfter(now) && dueAt.isAfter(now)) {
                Set<User> recipients = new HashSet<>(task.getAssignees());
                recipients.addAll(productOwners);
                if (recipients.isEmpty()) {
                    continue;
                }
                String message = "Task \"" + task.getTitle()
                        + "\" The Task is due soon (still ~"
                        + leadMinutes + " minuts, estimation: " + dueAt + ").";
                notificationService.notifyUsers(recipients, message, NotificationType.TASK_DUE_SOON);
                task.setReminderSentAt(LocalDateTime.now());
                taskRepository.save(task);
            }
        }
    }
}