package _Blog_Backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import _Blog_Backend.dto.CursorResponse;
import _Blog_Backend.dto.NotificationDto;
import _Blog_Backend.entity.Notification;
import _Blog_Backend.entity.Post;
import _Blog_Backend.entity.Subscription;
import _Blog_Backend.entity.User;
import _Blog_Backend.repository.NotificationRepository;
import _Blog_Backend.repository.SubscriptionRepository;
import _Blog_Backend.types.NotificationType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public void sendNotification(User receiver, User sender, NotificationType type) {
        if (receiver.getId().equals(sender.getId())) {
            return;
        }

        Notification notification = Notification.builder()
                .receiver(receiver)
                .sender(sender)
                .type(type)
                .build();

        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public int getUnreadNotificationsCount(User currentUser) {
        return notificationRepository.countByReceiverIdAndIsReadFalse(currentUser.getId());
    }

    @Transactional(readOnly = true)
    public CursorResponse<NotificationDto> getUserNotifications(User currentUser, Long cursor) {

        Pageable pageRequest = PageRequest.of(0, 11);
        List<Notification> notifications;

        if (cursor == null) {
            notifications = notificationRepository.findByReceiverIdOrderByIdDesc(currentUser.getId(), pageRequest);
        } else {
            notifications = notificationRepository.findByReceiverIdAndIdLessThanOrderByIdDesc(currentUser.getId(),
                    cursor, pageRequest);
        }

        Long nextCursor = null;
        boolean hasMore = notifications.size() > 10;

        if (hasMore) {
            notifications.remove(notifications.size() - 1);
            nextCursor = notifications.get(notifications.size() - 1).getId();
        }

        List<NotificationDto> dtos = notifications.stream()
                .map(n -> new NotificationDto(
                        n.getId(),
                        n.getSender().getUsername(),
                        n.getSender().getProfilePictureUrl(),
                        n.getType().name(),
                        n.getPost() != null ? n.getPost().getId() : null,
                        n.isRead(),
                        n.getCreatedAt()))
                .toList();

        return new CursorResponse<>(dtos, nextCursor, hasMore);
    }

    @Transactional
    public String markAsReadOrUnread(Long notificationId, User currentUser) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));

        if (!notification.getReceiver().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have permission to modify this notification.");
        }

        notification.setRead(!notification.isRead());

        notificationRepository.save(notification);

        return notification.isRead()
                ? "Notification marked as read successfully."
                : "Notification marked as unread successfully.";
    }

    @Transactional
    public void deleteNotification(User receiver, User sender, NotificationType type) {
        notificationRepository.deleteByReceiverIdAndSenderIdAndType(receiver.getId(), sender.getId(), type);
    }

    @Async
    @Transactional
    public void notifyFollowersOfNewPost(User author, Post newPost) {

        List<Subscription> followers = subscriptionRepository.findAllByTargetUserId(author.getId());

        if (followers.isEmpty()) {
            return;
        }

        List<Notification> notificationsToSave = new ArrayList<>();

        for (Subscription subscription : followers) {
            User subscriber = subscription.getSubscriber();

            Notification alert = Notification.builder()
                    .receiver(subscriber)
                    .sender(author)
                    .post(newPost)
                    .type(NotificationType.NEW_POST)
                    .build();

            notificationsToSave.add(alert);
        }

        notificationRepository.saveAll(notificationsToSave);
    }
}