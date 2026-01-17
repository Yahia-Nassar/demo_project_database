package com.example.demo.notification;

import com.example.demo.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);
    List<Notification> findByRecipientAndReadAtIsNullOrderByCreatedAtDesc(User recipient);
    Optional<Notification> findByIdAndRecipient(Long id, User recipient);
    void deleteByIdAndRecipient(Long id, User recipient);
    void deleteByRecipientAndReadAtIsNotNull(User recipient);
}