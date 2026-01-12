package com.example.demo.userstory;

import org.springframework.stereotype.Service;
import java.util.List;

import com.example.demo.user.User;
import com.example.demo.user.UserRepository;


@Service
public class UserStoryService {
    private final UserStoryRepository repo;
    private final UserRepository userRepo;
    
    public UserStoryService(UserStoryRepository repo, UserRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    public void removeDeveloper(Long storyId, Long userId) {
    UserStory story = repo.findById(storyId).orElseThrow();
    story.getDevelopers().removeIf(u -> u.getId().equals(userId));
    repo.save(story);
}


    public UserStory create(UserStory story) {
        story.setStatus(UserStoryStatus.BACKLOG);
        return repo.save(story);
    }

    public void moveToSprint(Long id) {
        UserStory story = repo.findById(id).orElseThrow();
        story.setStatus(UserStoryStatus.SPRINT);
        repo.save(story);
    }

    public void assignDevelopers(Long storyId, List<Long> userIds) {
    UserStory story = repo.findById(storyId).orElseThrow();
    List<User> devs = userRepo.findAllById(userIds);
    story.getDevelopers().clear();
    story.getDevelopers().addAll(devs);
    repo.save(story);
    }

    public List<UserStory> backlog() {
        return repo.findByStatus(UserStoryStatus.BACKLOG);
    }

    public List<UserStory> sprint() {
        return repo.findByStatus(UserStoryStatus.SPRINT);
    }

    public UserStory findById(Long id) {
    return repo.findById(id).orElseThrow();
    }

    public List<UserStory> storiesForDeveloper(Long userId) {
        return repo.findByDevelopers_Id(userId);
    }

}
