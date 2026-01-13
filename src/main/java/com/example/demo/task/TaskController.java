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
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("task", service.findById(id));
        model.addAttribute("developers", userService.findDevelopers());
        return "task-edit";
    }

    @PreAuthorize("hasRole('PO') or hasRole('DEVELOPER')")
    @PostMapping("/{id}/edit")
    public String edit(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam(name = "assignees", required = false) List<Long> assignees,
            @RequestParam(required = false) Double estimateHours,
            @RequestParam(required = false) Double actualHours
    ) {
        try {
            service.updateDetails(
                    id,
                    title,
                    assignees == null ? List.of() : assignees,
                    estimateHours,
                    actualHours
            );
            return "redirect:/board/sprint";
        } catch (OptimisticLockingFailureException ex) {
            return "redirect:/board/sprint?error=conflict";
        }
    }

    @PreAuthorize("hasRole('PO') or hasRole('DEVELOPER')")
    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam TaskStatus status,
            @RequestParam(defaultValue = "/board/sprint") String returnUrl
    ) {
        try {
            service.changeStatus(id, status);
            return "redirect:" + returnUrl;
        } catch (OptimisticLockingFailureException ex) {
            return "redirect:" + returnUrl + (returnUrl.contains("?") ? "&" : "?")
                    + "error=conflict";
        }
    }
}