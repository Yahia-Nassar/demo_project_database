package com.example.demo.controller;

import com.example.demo.task.Task;
import com.example.demo.task.TaskService;
import com.example.demo.task.TaskStatus;
import com.example.demo.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/board")
@PreAuthorize("hasRole('PO') or hasRole('DEVELOPER')")
public class SprintBoardController {

    private final TaskService taskService;
    private final UserService userService;

    public SprintBoardController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    @GetMapping("/sprint")
    public String sprintBoard(Model model) {
        List<Task> tasks = taskService.sprintTasks();

        Map<Long, TaskStatus> normalizedStatuses = tasks.stream()
                .collect(Collectors.toMap(Task::getId, task -> normalizeStatus(task.getStatus())));

        Map<TaskStatus, List<Task>> columns = new LinkedHashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            columns.put(status, tasks.stream()
                    .filter(task -> status.equals(normalizeStatus(task.getStatus())))
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
        model.addAttribute("returnUrl", "/board/sprint");
        return "sprint-board";
    }

    private TaskStatus normalizeStatus(TaskStatus status) {
        return status == null ? TaskStatus.TODO : status;
    }
}