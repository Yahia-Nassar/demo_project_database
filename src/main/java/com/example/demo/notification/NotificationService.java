package com.example.demo.notification;

import com.example.demo.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationService {

    private static final long SSE_TIMEOUT_MS = 0L;

    private final NotificationRepository repository;
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    public Notification createNotification(User recipient, String message, NotificationType type) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setMessage(message);
        notification.setType(type);
        notification.setCreatedAt(LocalDateTime.now());
        Notification saved = repository.save(notification);
        sendToUser(recipient.getId(), saved);
        return saved;
    }

    public void notifyUsers(Collection<User> recipients, String message, NotificationType type) {
        for (User user : recipients) {
            createNotification(user, message, type);
        }
    }

    public List<Notification> notificationsFor(User user) {
        return repository.findByRecipientOrderByCreatedAtDesc(user);
    }

    public long unreadCount(User user) {
        return repository.findByRecipientAndReadAtIsNullOrderByCreatedAtDesc(user).size();
    }

    @Transactional
    public void markRead(Long id, User user) {
        repository.findByIdAndRecipient(id, user).ifPresent(notification -> {
            if (notification.getReadAt() == null) {
                notification.setReadAt(LocalDateTime.now());
                repository.save(notification);
            }
        });
    }

    @Transactional
    public void deleteNotification(Long id, User user) {
        repository.findByIdAndRecipient(id, user)
                .ifPresent(repository::delete);
    }

    @Transactional
    public void deleteRead(User user) {
        repository.deleteByRecipientAndReadAtIsNotNull(user);
    }

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        emitters.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(ex -> removeEmitter(userId, emitter));
        return emitter;
    }

    private void sendToUser(Long userId, Notification notification) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null) {
            return;
        }
        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notification.getMessage()));
            } catch (IOException ex) {
                removeEmitter(userId, emitter);
            }
        }
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
        }
    }
}