package com.example.demo.dev;

import com.example.demo.user.UserService;
import com.example.demo.user.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.task.TaskService;

@Controller
@RequestMapping("/dev")
@PreAuthorize("hasRole('DEVELOPER')")
public class DeveloperDashboardController {

    private final TaskService taskService;
    private final UserService userService;

    public DeveloperDashboardController(TaskService taskService,
                                        UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User current = userService.currentUser();
        model.addAttribute("tasks", taskService.findByDeveloper(current));
        return "dev-dashboard";
    }
    
    @GetMapping("/dev/tasks")
    public String myTasks(Model model) {

        User dev = userService.currentUser();
        model.addAttribute("tasks", taskService.findByDeveloper(dev));

        return "dev-tasks";
}


}