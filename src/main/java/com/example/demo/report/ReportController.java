package com.example.demo.report;

import com.example.demo.task.Task;
import com.example.demo.task.TaskService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@PreAuthorize("hasRole('PO') or hasRole('DEVELOPER')")
public class ReportController {

    private final TaskService taskService;

    public ReportController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/reports/estimates")
    public String estimates(Model model) {
        List<Task> tasks = taskService.allTasks().stream()
                .filter(task -> task.getEstimateHours() != null || task.getActualHours() != null)
                .collect(Collectors.toList());
        List<String> labels = tasks.stream()
                .map(Task::getTitle)
                .collect(Collectors.toList());
        List<Double> estimates = tasks.stream()
                .map(Task::getEstimateHours)
                .collect(Collectors.toList());
        List<Double> actuals = tasks.stream()
                .map(Task::getActualHours)
                .collect(Collectors.toList());
        model.addAttribute("tasks", tasks);
        model.addAttribute("labels", labels);
        model.addAttribute("estimates", estimates);
        model.addAttribute("actuals", actuals);
        return "reports-estimates";
    }
}