package _Blog_Backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import _Blog_Backend.entity.Like;

public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByPostIdAndUserId(Long postId, Long userId);

    Optional<Like> findByCommentIdAndUserId(Long commentId, Long userId);

    long countByPostId(Long postId);

    long countByCommentId(Long commentId);

    @Query("SELECT l.post.id FROM Like l WHERE l.user.id = :userId AND l.post.id IN :postIds")
    List<Long> findLikedPostIdsByUser(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);
}