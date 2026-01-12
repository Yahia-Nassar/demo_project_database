package com.example.demo.userstory;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserStoryRepository extends JpaRepository<UserStory, Long> {

    List<UserStory> findByStatus(UserStoryStatus status);

    List<UserStory> findByDevelopers_Id(Long developerId);
}
