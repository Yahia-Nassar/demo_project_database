package com.example.demo.controller;

import com.example.demo.task.Task;
import com.example.demo.task.TaskService;
import com.example.demo.task.TaskStatus;
import com.example.demo.user.UserService;
import com.example.demo.userstory.UserStoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/board")
@PreAuthorize("hasRole('PO') or hasRole('DEVELOPER')")
public class SprintBoardPageController {

    private final TaskService taskService;
    private final UserService userService;
    private final UserStoryService userStoryService;

    public SprintBoardPageController(TaskService taskService, UserService userService, UserStoryService userStoryService) {
        this.taskService = taskService;
        this.userService = userService;
        this.userStoryService = userStoryService;
    }

    @GetMapping("/sprint")
    public String sprintBoard(
            Model model,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "assigneeId", required = false) Long assigneeId,
            @RequestParam(name = "status", required = false) TaskStatus status
    ) {
        List<Task> tasks = taskService.sprintTasks();
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase();
        if (!normalizedQuery.isEmpty()) {
            tasks = tasks.stream()
                    .filter(task -> matchesQuery(task, normalizedQuery))
                    .collect(Collectors.toList());
        }
        if (assigneeId != null) {
            tasks = tasks.stream()
                    .filter(task -> task.getAssignees().stream().anyMatch(user -> assigneeId.equals(user.getId())))
                    .collect(Collectors.toList());
        }

        Map<Long, TaskStatus> normalizedStatuses = tasks.stream()
                .collect(Collectors.toMap(Task::getId, task -> normalizeStatus(task.getStatus())));

        Map<TaskStatus, List<Task>> columns = new LinkedHashMap<>();
        for (TaskStatus columnStatus : TaskStatus.values()) {
            if (status != null && columnStatus != status) {
                continue;
            }
            columns.put(columnStatus, tasks.stream()
                    .filter(task -> columnStatus.equals(normalizeStatus(task.getStatus())))
                    .collect(Collectors.toList()));
        }

        Map<Long, List<TaskStatus>> transitions = tasks.stream()
                .collect(Collectors.toMap(
                        Task::getId,
                        task -> taskService.allowedTransitions(normalizeStatus(task.getStatus()))
                                .stream()
                                .toList()
                ));

        model.addAttribute("columns", columns);
        model.addAttribute("transitions", transitions);
        model.addAttribute("normalizedStatuses", normalizedStatuses);
        model.addAttribute("developers", userService.findDevelopers());
        model.addAttribute("stories", userStoryService.sprint());
        model.addAttribute("query", query);
        model.addAttribute("assigneeId", assigneeId);
        model.addAttribute("statusFilter", status);
        model.addAttribute("returnUrl", buildReturnUrl(query, assigneeId, status));
        return "sprint-board";
    }

    private TaskStatus normalizeStatus(TaskStatus status) {
        return status == null ? TaskStatus.TODO : status;
    }

    private boolean matchesQuery(Task task, String query) {
        if (task.getTitle() != null && task.getTitle().toLowerCase().contains(query)) {
            return true;
        }
        return task.getStory() != null
                && task.getStory().getTitle() != null
                && task.getStory().getTitle().toLowerCase().contains(query);
    }

    private String buildReturnUrl(String query, Long assigneeId, TaskStatus status) {
        StringBuilder url = new StringBuilder("/board/sprint");
        boolean hasParam = false;
        if (query != null && !query.isBlank()) {
            url.append(hasParam ? "&" : "?")
                    .append("query=")
                    .append(URLEncoder.encode(query, StandardCharsets.UTF_8));
            hasParam = true;
        }
        if (assigneeId != null) {
            url.append(hasParam ? "&" : "?").append("assigneeId=").append(assigneeId);
            hasParam = true;
        }
        if (status != null) {
            url.append(hasParam ? "&" : "?").append("status=").append(status);
        }
        return url.toString();
    }
}