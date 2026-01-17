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
    private final Map<Long, List<SseEmitter>> emittersById = new ConcurrentHashMap<>();
    private final Map<String, List<SseEmitter>> emittersByEmail = new ConcurrentHashMap<>();

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
        return repository.countByRecipientAndReadAtIsNull(user);
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
        emittersById.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(ex -> removeEmitter(userId, emitter));
        return emitter;
    }

    public SseEmitter subscribeByEmail(String email) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        emittersByEmail.computeIfAbsent(email, key -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(email, emitter));
        emitter.onTimeout(() -> removeEmitter(email, emitter));
        emitter.onError(ex -> removeEmitter(email, emitter));
        return emitter;
    }

    private void sendToUser(Long userId, Notification notification) {
        sendToEmitters(userId, notification);
        if (notification.getRecipient() != null && notification.getRecipient().getEmail() != null) {
            sendToEmitters(notification.getRecipient().getEmail(), notification);
        }
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emittersById.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
        }
    }

    private void removeEmitter(String email, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emittersByEmail.get(email);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
        }
    }

    private void sendToEmitters(Long userId, Notification notification) {
        List<SseEmitter> userEmitters = emittersById.get(userId);
        sendToEmitters(userEmitters, notification, emitter -> removeEmitter(userId, emitter));
    }

    private void sendToEmitters(String email, Notification notification) {
        List<SseEmitter> userEmitters = emittersByEmail.get(email);
        sendToEmitters(userEmitters, notification, emitter -> removeEmitter(email, emitter));
    }

    private void sendToEmitters(List<SseEmitter> userEmitters,
                                Notification notification,
                                java.util.function.Consumer<SseEmitter> cleanup) {
        if (userEmitters == null) {
            return;
        }
        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notification.getMessage()));
            } catch (IOException ex) {
                cleanup.accept(emitter);
            }
        }
    }
}