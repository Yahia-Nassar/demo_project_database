package com.example.demo.userstory;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.example.demo.user.UserService;
import java.util.List;

@Controller
@RequestMapping("/stories")
public class UserStoryController {

    private final UserStoryService service;
    private final UserService userService;


    public UserStoryController(
            UserStoryService service,
            UserService userService
    ) {
        this.service = service;
        this.userService = userService;
    }


    @GetMapping("/backlog")
    public String backlog(
            @RequestParam(name = "sort", required = false) String sort,
            Model model
    ) {
        model.addAttribute("stories", service.backlog(sort));
        model.addAttribute("sort", sort);
        return "backlog";
    }

    @PreAuthorize("hasRole('PO')")
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("story", new UserStory());
        return "story-form";
    }

    @PreAuthorize("hasRole('PO')")
    @PostMapping("/new")
    public String create(
            @Valid @ModelAttribute("story") UserStory story,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            return "story-form";
        }
        service.create(story);
        return "redirect:/stories/backlog";
    }

    @PreAuthorize("hasRole('PO')")
    @PostMapping("/{id}/sprint")
    public String moveToSprint(@PathVariable Long id) {
        service.moveToSprint(id);
        return "redirect:/stories/backlog";
    }

    @PreAuthorize("hasRole('PO')")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("story", service.findById(id));
        return "story-edit";
    }

    @PreAuthorize("hasRole('PO')")
    @PostMapping("/{id}/edit")
    public String edit(
            @PathVariable Long id,
            @Valid @ModelAttribute("story") UserStory story,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            return "story-edit";
        }
        service.update(id, story);
        return "redirect:/stories/backlog";
    }

    @PreAuthorize("hasRole('PO')")
    @GetMapping("/{id}/assign")
    public String assignForm(@PathVariable Long id, Model model) {
        model.addAttribute("story", service.findById(id));
        model.addAttribute("developers", userService.findDevelopers());
        return "assign-developers";
    }
    @PreAuthorize("hasRole('PO')")
    @PostMapping("/{id}/assign")
    public String assign(
            @PathVariable Long id,
            @RequestParam(name = "developers", required = false) List<Long> developers
    ) {
        if (developers != null) {
            service.assignDevelopers(id, developers);
        }
        return "redirect:/po/dashboard";
    }

    @PreAuthorize("hasRole('PO')")
    @PostMapping("/{storyId}/remove-dev/{userId}")
    public String removeDeveloper(
            @PathVariable Long storyId,
            @PathVariable Long userId
    ) {
        service.removeDeveloper(storyId, userId);
        return "redirect:/po/dashboard";
    }
}
