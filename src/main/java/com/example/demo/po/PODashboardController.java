package com.example.demo.po;

import com.example.demo.userstory.UserStoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/po")
@PreAuthorize("hasRole('PO')")
public class PODashboardController {

    private final UserStoryService service;

    public PODashboardController(UserStoryService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("backlog", service.backlog());
        model.addAttribute("sprint", service.sprint());
        return "po-dashboard";
    }
}
