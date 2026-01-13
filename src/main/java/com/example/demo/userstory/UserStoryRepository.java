package com.example.demo.userstory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface UserStoryRepository extends JpaRepository<UserStory, Long> {

    List<UserStory> findByStatus(UserStoryStatus status);
    List<UserStory> findByStatusOrderByPriorityAsc(UserStoryStatus status);
    List<UserStory> findByStatusOrderByPriorityDesc(UserStoryStatus status);
    @Query("""
        select s from UserStory s
        where s.status = :status
          and (lower(s.title) like lower(concat('%', :query, '%'))
               or lower(s.description) like lower(concat('%', :query, '%')))
        """)
    List<UserStory> searchBacklog(@Param("status") UserStoryStatus status,
                                  @Param("query") String query);

    List<UserStory> findByDevelopers_Id(Long developerId);
}