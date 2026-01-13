package com.example.demo.notification;

import com.example.demo.user.User;
import com.example.demo.user.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Controller
@RequestMapping("/sse")
@PreAuthorize("isAuthenticated()")
public class NotificationSseController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationSseController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping("/notifications")
    public SseEmitter notifications() {
        User user = userService.currentUser();
        return notificationService.subscribe(user.getId());
    }
}