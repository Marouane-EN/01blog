package _Blog_Backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import _Blog_Backend.entity.PostMedia;

public interface PostMediaRepository extends JpaRepository<PostMedia, Long> {
    Optional<PostMedia> findByMediaUrl(String mediaUrl);
}
