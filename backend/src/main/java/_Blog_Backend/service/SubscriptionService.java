package _Blog_Backend.service;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import _Blog_Backend.dto.SubscriptionResponse;
import _Blog_Backend.entity.Subscription;
import _Blog_Backend.entity.User;
import _Blog_Backend.repository.SubscriptionRepository;
import _Blog_Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public SubscriptionResponse toggleSubscription(String targetUsername, User currentUser) {

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
        } else {
            Subscription newSub = Subscription.builder()
                    .subscriber(currentUser)
                    .targetUser(targetUser)
                    .build();

            currentUser.addSubscription(newSub);
            subscriptionRepository.save(newSub);
            isNowSubscribed = true;
        }

        int currentFollowerCount = targetUser.getFollowers().size();

        return new SubscriptionResponse(isNowSubscribed, currentFollowerCount);
    }
}
