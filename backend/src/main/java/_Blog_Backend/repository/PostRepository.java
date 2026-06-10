package _Blog_Backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import _Blog_Backend.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query(value = "SELECT * FROM post WHERE is_hidden = true AND  id < ? ORDRE BY id DESC LIMIT 10", nativeQuery = true)
    List<Post> findAllHiddenPostsForAdmin();

    List<Post> findAllByOrderByIdDesc(Pageable pageable);

    List<Post> findByIdLessThanOrderByIdDesc(Long cursor, Pageable pageable);

    Optional<Post> findBySlug(String slug);
}
