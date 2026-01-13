package com.example.demo.notification;

import com.example.demo.user.User;
import com.example.demo.user.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping
    public String notifications(Model model) {
        User user = userService.currentUser();
        model.addAttribute("notifications", notificationService.notificationsFor(user));
        model.addAttribute("unreadCount", notificationService.unreadCount(user));
        return "notifications";
    }

    @PostMapping("/{id}/read")
    public String markRead(@PathVariable Long id) {
        notificationService.markRead(id, userService.currentUser());
        return "redirect:/notifications";
    }
}