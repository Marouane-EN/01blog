package _Blog_Backend.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import _Blog_Backend.entity.Notification;
import _Blog_Backend.types.NotificationType;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

        @Query("SELECT n FROM Notification n JOIN FETCH n.sender LEFT JOIN FETCH n.post WHERE n.receiver.id = :userId ORDER BY n.id DESC")
        List<Notification> findByReceiverIdOrderByIdDesc(@Param("userId") Long userId, Pageable pageable);

        @Query("SELECT n FROM Notification n JOIN FETCH n.sender LEFT JOIN FETCH n.post WHERE n.receiver.id = :userId AND n.id < :cursor ORDER BY n.id DESC")
        List<Notification> findByReceiverIdAndIdLessThanOrderByIdDesc(@Param("userId") Long userId,
                        @Param("cursor") Long cursor, Pageable pageable);

        int countByReceiverIdAndIsReadFalse(Long receiverId);

        void deleteByReceiverIdAndSenderIdAndType(Long receiverId, Long senderId,
                        NotificationType type);

}