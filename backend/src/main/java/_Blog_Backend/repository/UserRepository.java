package _Blog_Backend.repository;

import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import _Blog_Backend.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameOrEmail(String username, String Email);

    Optional<User> findByProfilePictureUrl(String profilePictureUrl);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // Bypasses the "is_blocked = false" restriction
    @Query(value = "SELECT * FROM users ORDER BY created_at DESC", countQuery = "SELECT count(*) FROM users", nativeQuery = true)
    Page<User> findAllForAdmin(Pageable pageable);

    Page<User> findByUsernameContainingIgnoreCase(String keyword, Pageable pageable);

    // In UserRepository.java

    // 1. Initial Search (No Cursor)
    @Query("SELECT u FROM users u WHERE " +
            "(u.username ILIKE %:keyword% OR u.bio ILIKE %:keyword%) " +
            "AND u.isActive = true AND u.isBlocked = false " +
            "ORDER BY u.id DESC")
    List<User> searchPublicUsers(@Param("keyword") String keyword, Pageable pageable);

    // 2. Cursor Search
    @Query("SELECT u FROM users u WHERE " +
            "(u.username ILIKE %:keyword% OR u.bio ILIKE %:keyword%) " +
            "AND u.isActive = true AND u.isBlocked = false " +
            "AND u.id < :cursor ORDER BY u.id DESC")
    List<User> searchPublicUsersByCursor(@Param("keyword") String keyword, @Param("cursor") Long cursor,
            Pageable pageable);
}
