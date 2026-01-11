package com.example.demo.userstory;

import com.example.demo.user.User;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

@Entity
public class UserStory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;

    private int priority;

    @Enumerated(EnumType.STRING)
    private UserStoryStatus status;

    @ManyToMany
    @JoinTable(
        name = "userstory_developers",
        joinColumns = @JoinColumn(name = "story_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> developers = new HashSet<>();
// getters & setters

    public Set<User> getDevelopers() {
        return developers;
    }

    public void setDevelopers(Set<User> developers) {
        this.developers = developers;
    }
    
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }

    public UserStoryStatus getStatus() {
        return status;
    }
    
    public void setStatus(UserStoryStatus status) {
        this.status = status;
    }
}

