package com.example.demo.userstory;

import org.springframework.stereotype.Service;
import java.util.List;

import com.example.demo.audit.AuditLogService;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;


@Service
public class UserStoryService {
    private final UserStoryRepository repo;
    private final UserRepository userRepo;
    private final AuditLogService auditLogService;
    
    public UserStoryService(UserStoryRepository repo, UserRepository userRepo, AuditLogService auditLogService) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.auditLogService = auditLogService;
    }

    public void removeDeveloper(Long storyId, Long userId) {
        UserStory story = repo.findById(storyId).orElseThrow();
        story.getDevelopers().removeIf(u -> u.getId().equals(userId));
        repo.save(story);
    }


    public UserStory create(UserStory story) {
        story.setStatus(UserStoryStatus.BACKLOG);
        UserStory saved = repo.save(story);
        auditLogService.record("UserStory", saved.getId(), "CREATED", "User story created");
        return saved;
    }

    public void moveToSprint(Long id) {
        UserStory story = repo.findById(id).orElseThrow();
        story.setStatus(UserStoryStatus.SPRINT);
        repo.save(story);
        auditLogService.record("UserStory", story.getId(), "STATUS_CHANGED", "Moved to sprint");
    }

    public void assignDevelopers(Long storyId, List<Long> userIds) {
        UserStory story = repo.findById(storyId).orElseThrow();
        List<User> devs = userRepo.findAllById(userIds);
        story.getDevelopers().clear();
        story.getDevelopers().addAll(devs);
        repo.save(story);
        auditLogService.record("UserStory", story.getId(), "ASSIGNED", "Developers assigned");
    }

    public List<UserStory> backlog() {
        return repo.findByStatus(UserStoryStatus.BACKLOG);
    }

    public List<UserStory> backlog(String sort) {
        return backlog(sort, null);
    }

    public List<UserStory> backlog(String sort, String query) {
        if (query != null && !query.isBlank()) {
            return repo.searchBacklog(UserStoryStatus.BACKLOG, query.trim());
        }
        if ("priority_asc".equalsIgnoreCase(sort)) {
            return repo.findByStatusOrderByPriorityAsc(UserStoryStatus.BACKLOG);
        }
        if ("priority_desc".equalsIgnoreCase(sort)) {
            return repo.findByStatusOrderByPriorityDesc(UserStoryStatus.BACKLOG);
        }
        return repo.findByStatus(UserStoryStatus.BACKLOG);
    }

    public List<UserStory> sprint() {
        return repo.findByStatus(UserStoryStatus.SPRINT);
    }

    public UserStory findById(Long id) {
        return repo.findById(id).orElseThrow();
    }

    public UserStory update(Long id, UserStory updated) {
        UserStory story = repo.findById(id).orElseThrow();
        story.setTitle(updated.getTitle());
        story.setDescription(updated.getDescription());
        story.setPriority(updated.getPriority());
        UserStory saved = repo.save(story);
        auditLogService.record("UserStory", saved.getId(), "UPDATED", "User story updated");
        return saved;
    }

    public List<UserStory> storiesForDeveloper(Long userId) {
        return repo.findByDevelopers_Id(userId);
    }

}