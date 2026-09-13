package _Blog_Backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import _Blog_Backend.dto.AdminPostProjection;
import _Blog_Backend.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
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

        @Query(value = "SELECT * FROM posts WHERE id = :postId", nativeQuery = true)
        Optional<Post> findPostById(@Param("postId") Long postId);

        @EntityGraph(attributePaths = { "author" })
        Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

        @EntityGraph(attributePaths = { "author" })
        Page<Post> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String keyword, Pageable pageable);

        // FOR THE PUBLIC: Hibernate automatically applies "is_hidden = false"
        @EntityGraph(attributePaths = { "author" })
        Page<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

        // =====================================================================
        // ADMIN NATIVE QUERIES (Bypasses @SQLRestriction)
        // Uses AdminPostProjection to prevent @Formula crashes!
        // =====================================================================

        // 1. Find All For Admin
        @Query(value = "SELECT p.id as id, p.title as title, u.username as authorUsername, " +
                        "u.profile_picture_url as authorProfilePictureUrl, p.is_hidden as isHidden, " +
                        "p.created_at as createdAt, " +
                        "(SELECT COUNT(*) FROM likes l WHERE l.post_id = p.id) as likesCount, " +
                        "(SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id AND c.is_deleted = false) as commentsCount "
                        +
                        "FROM posts p JOIN users u ON p.user_id = u.id " +
                        "ORDER BY p.created_at DESC", countQuery = "SELECT count(*) FROM posts", nativeQuery = true)
        Page<AdminPostProjection> findAllForAdmin(Pageable pageable);

        // 2. Search All For Admin
        @Query(value = "SELECT p.id as id, p.title as title, u.username as authorUsername, " +
                        "u.profile_picture_url as authorProfilePictureUrl, p.is_hidden as isHidden, " +
                        "p.created_at as createdAt, " +
                        "(SELECT COUNT(*) FROM likes l WHERE l.post_id = p.id) as likesCount, " +
                        "(SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id AND c.is_deleted = false) as commentsCount "
                        +
                        "FROM posts p JOIN users u ON p.user_id = u.id " +
                        "WHERE p.title ILIKE %:keyword% " +
                        "ORDER BY p.created_at DESC", countQuery = "SELECT count(*) FROM posts WHERE title ILIKE %:keyword%", nativeQuery = true)
        Page<AdminPostProjection> searchAllForAdmin(@Param("keyword") String keyword, Pageable pageable);

        // 3. Find By Author ID For Admin
        @Query(value = "SELECT p.id as id, p.title as title, u.username as authorUsername, " +
                        "u.profile_picture_url as authorProfilePictureUrl, p.is_hidden as isHidden, " +
                        "p.created_at as createdAt, " +
                        "(SELECT COUNT(*) FROM likes l WHERE l.post_id = p.id) as likesCount, " +
                        "(SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id AND c.is_deleted = false) as commentsCount "
                        +
                        "FROM posts p JOIN users u ON p.user_id = u.id " +
                        "WHERE p.user_id = :authorId ORDER BY p.created_at DESC", countQuery = "SELECT count(*) FROM posts WHERE user_id = :authorId", nativeQuery = true)
        Page<AdminPostProjection> findByAuthorIdForAdmin(@Param("authorId") Long authorId, Pageable pageable);

        @Query("SELECT p FROM Post p WHERE p.author.id = :authorId AND p.is_hidden = false ORDER BY p.id DESC")
        List<Post> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);

        @Query("SELECT p FROM Post p WHERE p.author.id = :authorId AND p.is_hidden = false AND p.id < :cursor ORDER BY p.id DESC")
        List<Post> findByAuthorIdAndCursor(@Param("authorId") Long authorId, @Param("cursor") Long cursor,
                        Pageable pageable);

        @Query("SELECT p FROM Post p WHERE p.is_hidden = false AND p.author.id IN " +
                        "(SELECT s.targetUser.id FROM Subscription s WHERE s.subscriber.id = :userId) " +
                        "ORDER BY p.id DESC")
        List<Post> findSubscriptionsFeed(@Param("userId") Long userId, Pageable pageable);

        @Query("SELECT p FROM Post p WHERE p.author.id IN " +
                        "(SELECT s.targetUser.id FROM Subscription s WHERE s.subscriber.id = :userId) " +
                        "AND p.is_hidden = false AND p.id < :cursor ORDER BY p.id DESC")
        List<Post> findSubscriptionsFeedByCursor(@Param("userId") Long userId, @Param("cursor") Long cursor,
                        Pageable pageable);

        @Query("SELECT p FROM Post p WHERE p.is_hidden = false AND " +
                        "(p.title ILIKE %:keyword% OR p.description ILIKE %:keyword%) " +
                        "ORDER BY p.id DESC")
        List<Post> searchPublicPosts(@Param("keyword") String keyword, Pageable pageable);

        @Query("SELECT p FROM Post p WHERE p.is_hidden = false AND " +
                        "(p.title ILIKE %:keyword% OR p.description ILIKE %:keyword%) " +
                        "AND p.id < :cursor ORDER BY p.id DESC")
        List<Post> searchPublicPostsByCursor(@Param("keyword") String keyword, @Param("cursor") Long cursor,
                        Pageable pageable);

        @Modifying
        @Query(value = "UPDATE posts SET is_hidden = :isHidden WHERE id = :postId", nativeQuery = true)
        void updatePostVisibilityForAdmin(@Param("postId") Long postId, @Param("isHidden") boolean isHidden);

        @Query(value = "SELECT p.*, " +
                        "(SELECT COUNT(*) FROM likes l WHERE l.post_id = p.id) as likesCount, " +
                        "(SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id AND c.is_deleted = false) as commentsCount "
                        +
                        "FROM posts p WHERE p.id = :postId", nativeQuery = true)
        Optional<Post> findPostEntityByIdForAdmin(@Param("postId") Long postId);

        boolean existsByIdAndIsHiddenFalse(Long postId);
}
