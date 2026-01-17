package com.example.demo.task;

import com.example.demo.audit.AuditLogService;
import com.example.demo.board.BoardEventService;
import com.example.demo.notification.NotificationService;
import com.example.demo.notification.NotificationType;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import com.example.demo.security.Role;
import com.example.demo.userstory.UserStory;
import com.example.demo.userstory.UserStoryStatus;
import com.example.demo.userstory.UserStoryRepository;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.time.LocalDateTime;

@Service
public class TaskService {

    private final TaskRepository taskRepo;
    private final UserStoryRepository storyRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;
    private final BoardEventService boardEventService;
    private final AuditLogService auditLogService;

    public TaskService(
            TaskRepository taskRepo,
            UserStoryRepository storyRepo,
            UserRepository userRepo,
            NotificationService notificationService,
            BoardEventService boardEventService,
            AuditLogService auditLogService
    ) {
        this.taskRepo = taskRepo;
        this.storyRepo = storyRepo;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
        this.boardEventService = boardEventService;
        this.auditLogService = auditLogService;
    }

    public Task create(Long storyId, String title) {
        UserStory story = storyRepo.findById(storyId).orElseThrow();

        Task task = new Task();
        task.setTitle(title);
        task.setStatus(TaskStatus.TODO);
        task.setPriority(3);
        task.setCreatedAt(LocalDateTime.now());
        task.setStory(story);

        Task saved = taskRepo.save(task);
        auditLogService.record("Task", saved.getId(), "CREATED", "Task created");
        boardEventService.broadcastBoardUpdate();
        return saved;
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
        notifyStakeholders(
                users,
                "Task assigned: " + task.getTitle(),
                NotificationType.TASK_ASSIGNED
        );
        auditLogService.record("Task", task.getId(), "ASSIGNED", "Task assignees updated");
        boardEventService.broadcastBoardUpdate();
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
        Task saved = taskRepo.save(task);
        boardEventService.broadcastBoardUpdate();
        auditLogService.record("Task", saved.getId(), "UPDATED", "Task updated");
        return saved;
    }

    public Task updateDetails(Long taskId,
                              String title,
                              List<Long> assigneeIds,
                              Double estimateHours,
                              Double actualHours,
                              Integer priority,
                              String startedAt,
                              Integer reminderLeadMinutes) {
        Task task = taskRepo.findById(taskId).orElseThrow();
        Double previousEstimate = task.getEstimateHours();
        LocalDateTime previousStartedAt = task.getStartedAt();
        Integer previousLeadMinutes = task.getReminderLeadMinutes();
        task.setTitle(title);
        task.setEstimateHours(estimateHours);
        task.setActualHours(actualHours);
        if (priority != null) {
            task.setPriority(priority);
        }
        LocalDateTime parsedStartedAt = parseStartedAt(startedAt);
        if (parsedStartedAt != null) {
            task.setStartedAt(parsedStartedAt);
        }
        if (reminderLeadMinutes != null) {
            task.setReminderLeadMinutes(reminderLeadMinutes);
        }
        if (estimateHours != null && !estimateHours.equals(previousEstimate)) {
            task.setReminderSentAt(null);
        }
        if (parsedStartedAt != null && !parsedStartedAt.equals(previousStartedAt)) {
            task.setReminderSentAt(null);
        }
        if (reminderLeadMinutes != null && !reminderLeadMinutes.equals(previousLeadMinutes)) {
            task.setReminderSentAt(null);
        }
        if (assigneeIds != null) {
            List<User> users = userRepo.findAllById(assigneeIds);
            task.getAssignees().clear();
            task.getAssignees().addAll(users);
        }
        Task saved = taskRepo.save(task);
        notifyStakeholders(
                task.getAssignees(),
                "Task updated: " + task.getTitle(),
                NotificationType.TASK_UPDATED
        );
        auditLogService.record("Task", saved.getId(), "UPDATED", "Task details updated");
        boardEventService.broadcastBoardUpdate();
        return saved;
    }

    public Task updateDetails(Long taskId,
                              String title,
                              List<Long> assigneeIds,
                              Double estimateHours,
                              Double actualHours,
                              Integer priority) {
        return updateDetails(taskId, title, assigneeIds, estimateHours, actualHours, priority, null, null);
    }

    public void markDone(Long taskId) {
        changeStatus(taskId, TaskStatus.DONE);
    }

    public void changeStatus(Long taskId, TaskStatus targetStatus) {
        if (targetStatus == null) {
            throw new IllegalArgumentException("Target status must be provided");
        }
        Task task = taskRepo.findById(taskId).orElseThrow();
        TaskStatus current = task.getStatus();
        if (!allowedTransitions(current).contains(targetStatus)) {
            throw new IllegalStateException(
                    "Invalid status transition from " + current + " to " + targetStatus
            );
        }
        task.setStatus(targetStatus);
        if (targetStatus == TaskStatus.IN_PROGRESS && task.getStartedAt() == null) {
            task.setStartedAt(LocalDateTime.now());
            task.setReminderSentAt(null);
        }
        if (targetStatus == TaskStatus.DONE) {
            task.setCompletedAt(LocalDateTime.now());
        } else if (current == TaskStatus.DONE) {
            task.setCompletedAt(null);
        }
        taskRepo.save(task);
        notifyStakeholders(
                task.getAssignees(),
                "Task \"" + task.getTitle() + "\" moved to " + targetStatus,
                NotificationType.TASK_STATUS_CHANGED
        );
        auditLogService.record("Task", task.getId(), "STATUS_CHANGED", "Status changed to " + targetStatus);
        boardEventService.broadcastBoardUpdate();
    }

    public Set<TaskStatus> allowedTransitions(TaskStatus currentStatus) {
         return EnumSet.allOf(TaskStatus.class);
    }

    private LocalDateTime parseStartedAt(String startedAt) {
        if (startedAt == null || startedAt.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(startedAt);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private void notifyStakeholders(List<User> assignees, String message, NotificationType type) {
        Set<User> recipients = new HashSet<>(assignees == null ? List.of() : assignees);
        recipients.addAll(userRepo.findByRole(Role.PO));
        if (!recipients.isEmpty()) {
            notificationService.notifyUsers(recipients, message, type);
        }
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

    public List<Task> allTasks() {
        return taskRepo.findAll();
    }
}