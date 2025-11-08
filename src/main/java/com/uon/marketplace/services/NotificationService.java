package com.uon.marketplace.services;

import com.uon.marketplace.entities.AppUser;
import com.uon.marketplace.entities.Notification;
import com.uon.marketplace.repositories.AppUserRepository;
import com.uon.marketplace.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final AppUserRepository appUserRepository;
    private final EmailService emailService;

    @Value("${notifications.email.enabled:true}")
    private boolean emailEnabled;

    public NotificationService(NotificationRepository notificationRepository,
                               AppUserRepository appUserRepository,
                               EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.appUserRepository = appUserRepository;
        this.emailService = emailService;
    }

    /**
     * Create an in-app notification. Overload without email flag keeps default behavior (no email).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notification create(Long userId, String type, String title, String body) {
        return create(userId, type, title, body, false);
    }

    /**
     * Create a notification and optionally send an email to the user.
     * Uses REQUIRES_NEW so that notifications persist even if caller transaction rolls back.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notification create(Long userId, String type, String title, String body, boolean sendEmail) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setBody(body);
        n.setCreatedAt(LocalDateTime.now());
        Notification saved = notificationRepository.save(n);

        if (emailEnabled && sendEmail) {
            try {
                AppUser user = appUserRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found for notification"));
                if (user.getEmail() != null && !user.getEmail().isBlank()) {
                    emailService.send(user.getEmail(), title, body);
                }
            } catch (Exception e) {
                // Don't interrupt business flow on email failure
                System.err.println("Failed to send moderation email: " + e.getMessage());
            }
        }

        return saved;
    }

    public List<Notification> list(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    public Notification markRead(Long notificationId, Long userId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (!n.getUserId().equals(userId)) {
            throw new RuntimeException("Not authorized to modify this notification");
        }
        if (Boolean.FALSE.equals(n.getRead())) {
            n.setRead(true);
            n.setReadAt(LocalDateTime.now());
            notificationRepository.save(n);
        }
        return n;
    }

    public int markAllRead(Long userId) {
        List<Notification> all = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        int updated = 0;
        for (Notification n : all) {
            if (Boolean.FALSE.equals(n.getRead())) {
                n.setRead(true);
                n.setReadAt(LocalDateTime.now());
                notificationRepository.save(n);
                updated++;
            }
        }
        return updated;
    }
}
