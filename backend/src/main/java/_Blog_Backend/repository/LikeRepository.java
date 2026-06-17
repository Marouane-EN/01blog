package _Blog_Backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import _Blog_Backend.entity.Like;

public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByPostIdAndUserId(Long postId, Long userId);

    long countByPostId(Long postId);
}