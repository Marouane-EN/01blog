package _Blog_Backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import _Blog_Backend.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
        @Query(value = "SELECT * FROM post WHERE is_hidden = true AND  id < ? ORDRE BY id DESC LIMIT 10", nativeQuery = true)
        List<Post> findAllHiddenPostsForAdmin();

        @Query("SELECT p FROM Post p JOIN FETCH p.author a " +
                        "WHERE p.isHidden = false " +
                        "AND a.isBlocked = false AND a.isActive = true " +
                        "ORDER BY p.id DESC")
        List<Post> findPublicFeed(Pageable pageable);

        @Query("SELECT p FROM Post p JOIN FETCH p.author a " +
                        "WHERE p.id < :cursor " +
                        "AND p.isHidden = false " +
                        "AND a.isBlocked = false AND a.isActive = true " +
                        "ORDER BY p.id DESC")
        List<Post> findPublicFeedByCursor(@Param("cursor") Long cursor, Pageable pageable);

        Optional<Post> findBySlug(String slug);

        @Query(value = "SELECT * FROM posts ORDER BY created_at DESC", countQuery = "SELECT count(*) FROM posts", nativeQuery = true)
        Page<Post> findAllForAdmin(Pageable pageable);

        @Query(value = "SELECT * FROM posts WHERE id = :postId", nativeQuery = true)
        Optional<Post> findPostById(@Param("postId") Long postId);

        @Query(value = "SELECT * FROM posts WHERE title ILIKE %:keyword% ORDER BY created_at DESC", countQuery = "SELECT count(*) FROM posts WHERE title ILIKE %:keyword%", nativeQuery = true)
        Page<Post> searchAllForAdmin(@Param("keyword") String keyword, Pageable pageable);

        @EntityGraph(attributePaths = { "author" })
        Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

        @EntityGraph(attributePaths = { "author" })
        Page<Post> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String keyword, Pageable pageable);

        // FOR THE PUBLIC: Hibernate automatically applies "is_hidden = false"
        @EntityGraph(attributePaths = { "author" })
        Page<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

        // FOR ADMINS: Native SQL bypasses the bouncer to show hidden posts too!
        @Query(value = "SELECT * FROM posts WHERE author_id = :authorId ORDER BY created_at DESC", countQuery = "SELECT count(*) FROM posts WHERE author_id = :authorId", nativeQuery = true)
        Page<Post> findByAuthorIdForAdmin(@Param("authorId") Long authorId, Pageable pageable);
}
