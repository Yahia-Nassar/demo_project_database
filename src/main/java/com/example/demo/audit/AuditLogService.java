package com.example.demo.audit;

import com.example.demo.user.User;
import com.example.demo.user.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditLogService {

    private final AuditLogRepository repository;
    private final UserService userService;

    public AuditLogService(AuditLogRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    public void record(String entityType, Long entityId, String action, String details) {
        AuditLog log = new AuditLog();
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setDetails(details);
        log.setCreatedAt(LocalDateTime.now());
        log.setActor(safeCurrentUser());
        repository.save(log);
    }

    private User safeCurrentUser() {
        try {
            return userService.currentUser();
        } catch (RuntimeException ex) {
            return null;
        }
    }
}