package _Blog_Backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import _Blog_Backend.dto.UserDto;
import _Blog_Backend.dto.CursorResponse;
import _Blog_Backend.dto.SubscriptionResponse;
import _Blog_Backend.entity.Subscription;
import _Blog_Backend.entity.User;
import _Blog_Backend.repository.SubscriptionRepository;
import _Blog_Backend.repository.UserRepository;
import _Blog_Backend.types.NotificationType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationService notificationService;

    @Transactional
    public SubscriptionResponse toggleSubscription(String targetUsername, User detachedCurrentUser) {
        User currentUser = userRepository.findById(detachedCurrentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Current user not found"));
        User targetUser = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (targetUser.getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot subscribe to yourself.");
        }

        Optional<Subscription> existingSub = subscriptionRepository.findBySubscriberIdAndTargetUserId(
                currentUser.getId(), targetUser.getId());

        boolean isNowSubscribed;

        if (existingSub.isPresent()) {
            Subscription subToRemove = existingSub.get();
            currentUser.removeSubscription(subToRemove);

            subscriptionRepository.delete(subToRemove);
            isNowSubscribed = false;
            notificationService.deleteNotification(targetUser, currentUser, NotificationType.FOLLOW);

        } else {
            Subscription newSub = Subscription.builder()
                    .subscriber(currentUser)
                    .targetUser(targetUser)
                    .build();

            currentUser.addSubscription(newSub);
            subscriptionRepository.save(newSub);
            isNowSubscribed = true;
            notificationService.sendNotification(targetUser, currentUser, NotificationType.FOLLOW);
        }

        int currentFollowerCount = targetUser.getFollowers().size();

        return new SubscriptionResponse(isNowSubscribed, currentFollowerCount);
    }

    @Transactional(readOnly = true)
    public CursorResponse<UserDto> getFollowers(Long userId, Long cursor) {

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        Pageable pageRequest = PageRequest.of(0, 11);
        List<Subscription> subscriptions;

        if (cursor == null) {
            subscriptions = subscriptionRepository.findFollowersByTargetUserIdOrderByIdDesc(userId, pageRequest);
        } else {
            subscriptions = subscriptionRepository.findFollowersByTargetUserIdAndIdLessThanOrderByIdDesc(userId, cursor,
                    pageRequest);
        }

        Long nextCursor = null;
        boolean hasMore = subscriptions.size() > 10;

        if (hasMore) {
            subscriptions.remove(subscriptions.size() - 1);
            nextCursor = subscriptions.get(subscriptions.size() - 1).getId();
        }

        List<UserDto> followerDtos = subscriptions.stream()
                .map(sub -> mapToProfileDto(sub.getSubscriber()))
                .toList();

        return new CursorResponse<>(followerDtos, nextCursor, hasMore);
    }

    @Transactional(readOnly = true)
    public CursorResponse<UserDto> getFollowing(Long userId, Long cursor) {

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        Pageable pageRequest = PageRequest.of(0, 11);
        List<Subscription> subscriptions;

        if (cursor == null) {
            subscriptions = subscriptionRepository.findFollowingBySubscriberIdOrderByIdDesc(userId, pageRequest);
        } else {
            subscriptions = subscriptionRepository.findFollowingBySubscriberIdAndIdLessThanOrderByIdDesc(userId, cursor,
                    pageRequest);
        }

        Long nextCursor = null;
        boolean hasMore = subscriptions.size() > 10;

        if (hasMore) {
            subscriptions.remove(subscriptions.size() - 1);
            nextCursor = subscriptions.get(subscriptions.size() - 1).getId();
        }

        List<UserDto> followingDtos = subscriptions.stream()
                .map(sub -> mapToProfileDto(sub.getTargetUser()))
                .toList();

        return new CursorResponse<>(followingDtos, nextCursor, hasMore);
    }

    private UserDto mapToProfileDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getProfilePictureUrl());
    }
}
