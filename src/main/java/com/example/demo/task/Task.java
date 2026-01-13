package com.example.demo.task;

import com.example.demo.user.User;
import com.example.demo.userstory.UserStory;
import jakarta.persistence.*;

import java.util.HashSet;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Version
    private Long version;

    private Double estimateHours;
    private Double actualHours;
    private LocalDateTime completedAt;

    @ManyToOne(optional = false)
    private UserStory story;

    @ManyToMany
    @JoinTable(
            name = "task_assignees",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> assignees = new HashSet<>();

    // ===== GETTERS & SETTERS =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Double getEstimateHours() {
        return estimateHours;
    }

    public void setEstimateHours(Double estimateHours) {
        this.estimateHours = estimateHours;
    }

    public Double getActualHours() {
        return actualHours;
    }

    public void setActualHours(Double actualHours) {
        this.actualHours = actualHours;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public UserStory getStory() {
        return story;
    }

    public void setStory(UserStory story) {
        this.story = story;
    }

    public Set<User> getAssignees() {
        return assignees;
    }

    public void setAssignees(Set<User> assignees) {
        this.assignees = assignees;
    }
}