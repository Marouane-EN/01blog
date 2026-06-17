package _Blog_Backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import _Blog_Backend.entity.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    boolean existsBySubscriberIdAndTargetUserId(Long subscriberId, Long targetUserId);

    Optional<Subscription> findBySubscriberIdAndTargetUserId(Long subscriberId, Long targetUserId);
}