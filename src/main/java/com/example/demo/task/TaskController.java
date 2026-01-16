package com.example.demo.task;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.OptimisticLockingFailureException;

import com.example.demo.user.UserService;

import java.util.List;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService service;
    private final UserService userService;

    public TaskController(TaskService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    
    @PreAuthorize("hasRole('PO')")
    @PostMapping("/story/{storyId}")
    public String create(
            @PathVariable Long storyId,
            @RequestParam String title
    ) {
        service.create(storyId, title);
        return "redirect:/po/dashboard";
    }

    @PreAuthorize("hasRole('PO')")
    @PostMapping("/story")
    public String createFromBoard(
            @RequestParam Long storyId,
            @RequestParam String title
    ) {
        service.create(storyId, title);
        return "redirect:/board/sprint";
    }

    
    @PreAuthorize("hasRole('DEVELOPER')")
    @PostMapping("/{id}/done")
    public String done(@PathVariable Long id) {
        service.markDone(id);
        return "redirect:/dev/dashboard";
    }

    @PreAuthorize("hasRole('PO') or hasRole('DEVELOPER')")
    @PostMapping("/{id}/assign")
    public String assign(
            @PathVariable Long id,
            @RequestParam(name = "assignees", required = false) List<Long> assignees,
            @RequestParam(defaultValue = "/board/sprint") String returnUrl
    ) {
        try {
            service.assign(id, assignees == null ? List.of() : assignees);
            return "redirect:" + returnUrl;
        } catch (OptimisticLockingFailureException ex) {
            return "redirect:" + returnUrl + (returnUrl.contains("?") ? "&" : "?")
                    + "error=conflict";
        }
    }

    @PreAuthorize("hasRole('PO') or hasRole('DEVELOPER')")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @RequestParam(required = false) String returnUrl,
                           Model model) {
        model.addAttribute("task", service.findById(id));
        model.addAttribute("developers", userService.findDevelopers());
        model.addAttribute("returnUrl", returnUrl == null || returnUrl.isBlank()
                ? "/tasks/" + id + "/edit"
                : returnUrl);
        return "task-edit";
    }

    @PreAuthorize("hasRole('PO') or hasRole('DEVELOPER')")
    @PostMapping("/{id}/edit")
    public String edit(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam(name = "assignees", required = false) List<Long> assignees,
            @RequestParam(required = false) Double estimateHours,
            @RequestParam(required = false) Double actualHours,
            @RequestParam(required = false) String returnUrl
    ) {
        try {
            service.updateDetails(
                    id,
                    title,
                    assignees == null ? List.of() : assignees,
                    estimateHours,
                    actualHours
            );
            String redirectTarget = (returnUrl == null || returnUrl.isBlank())
                    ? "/tasks/" + id + "/edit"
                    : returnUrl;
            return "redirect:" + redirectTarget;
        } catch (OptimisticLockingFailureException ex) {
            String redirectTarget = (returnUrl == null || returnUrl.isBlank())
                    ? "/tasks/" + id + "/edit?error=conflict"
                    : returnUrl + (returnUrl.contains("?") ? "&" : "?") + "error=conflict";
            return "redirect:" + redirectTarget;
        }
    }

    @PreAuthorize("hasRole('PO') or hasRole('DEVELOPER')")
    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam("status") String statusValue,
            @RequestParam(defaultValue = "/board/sprint") String returnUrl
    ) {
        TaskStatus status;
        try {
            status = TaskStatus.valueOf(statusValue);
        } catch (IllegalArgumentException | NullPointerException ex) {
            return "redirect:" + returnUrl + (returnUrl.contains("?") ? "&" : "?")
                    + "error=invalid_status";
        }
        try {
            service.changeStatus(id, status);
            return "redirect:" + returnUrl;
        } catch (OptimisticLockingFailureException ex) {
            return "redirect:" + returnUrl + (returnUrl.contains("?") ? "&" : "?")
                    + "error=conflict";
        } catch (RuntimeException ex) {
            return "redirect:" + returnUrl + (returnUrl.contains("?") ? "&" : "?")
                    + "error=invalid_status";
        }
    }
}