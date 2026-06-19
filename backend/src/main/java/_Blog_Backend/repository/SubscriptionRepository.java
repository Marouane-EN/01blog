package _Blog_Backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import _Blog_Backend.entity.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

        boolean existsBySubscriberIdAndTargetUserId(Long subscriberId, Long targetUserId);

        Optional<Subscription> findBySubscriberIdAndTargetUserId(Long subscriberId, Long targetUserId);

        @Query("SELECT s FROM Subscription s JOIN FETCH s.subscriber WHERE s.targetUser.id = :userId ORDER BY s.id DESC")
        List<Subscription> findFollowersByTargetUserIdOrderByIdDesc(@Param("userId") Long userId, Pageable pageable);

        @Query("SELECT s FROM Subscription s JOIN FETCH s.subscriber WHERE s.targetUser.id = :userId AND s.id < :cursor ORDER BY s.id DESC")
        List<Subscription> findFollowersByTargetUserIdAndIdLessThanOrderByIdDesc(@Param("userId") Long userId,
                        @Param("cursor") Long cursor, Pageable pageable);

        @Query("SELECT s FROM Subscription s JOIN FETCH s.targetUser WHERE s.subscriber.id = :userId ORDER BY s.id DESC")
        List<Subscription> findFollowingBySubscriberIdOrderByIdDesc(@Param("userId") Long userId, Pageable pageable);

        @Query("SELECT s FROM Subscription s JOIN FETCH s.targetUser WHERE s.subscriber.id = :userId AND s.id < :cursor ORDER BY s.id DESC")
        List<Subscription> findFollowingBySubscriberIdAndIdLessThanOrderByIdDesc(@Param("userId") Long userId,
                        @Param("cursor") Long cursor, Pageable pageable);

        List<Subscription> findAllByTargetUserId(Long targetUserId);
}