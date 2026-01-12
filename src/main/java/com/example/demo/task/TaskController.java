package com.example.demo.task;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
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
}
