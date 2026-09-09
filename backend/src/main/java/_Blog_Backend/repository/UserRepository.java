package _Blog_Backend.repository;

import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import _Blog_Backend.dto.PopularUserProjection;
import _Blog_Backend.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
        Optional<User> findByEmail(String email);

        Optional<User> findByUsername(String username);

        Optional<User> findByUsernameOrEmail(String username, String Email);

        Optional<User> findByProfilePictureUrl(String profilePictureUrl);

        boolean existsByUsername(String username);

        boolean existsByEmail(String email);

        @Query(value = "SELECT * FROM users ORDER BY created_at DESC", countQuery = "SELECT count(*) FROM users", nativeQuery = true)
        Page<User> findAllForAdmin(Pageable pageable);

        Page<User> findByUsernameContainingIgnoreCase(String keyword, Pageable pageable);

        @Query("SELECT u FROM User u WHERE " +
                        "(u.username ILIKE %:keyword% OR u.bio ILIKE %:keyword%) " +
                        "AND u.isActive = true AND u.isBlocked = false " +
                        "ORDER BY u.id DESC")
        List<User> searchPublicUsers(@Param("keyword") String keyword, Pageable pageable);

        @Query("SELECT u FROM User u WHERE " +
                        "(u.username ILIKE %:keyword% OR u.bio ILIKE %:keyword%) " +
                        "AND u.isActive = true AND u.isBlocked = false " +
                        "AND u.id < :cursor ORDER BY u.id DESC")
        List<User> searchPublicUsersByCursor(@Param("keyword") String keyword, @Param("cursor") Long cursor,
                        Pageable pageable);

        // Most-followed active accounts. Joins the (correctly-named) `followers`
        // OneToMany directly instead of the User.followersCount/followingCount
        // @Formula fields, whose SQL is swapped relative to their names.
        @Query("SELECT u.id AS id, u.username AS username, u.profilePictureUrl AS profilePictureUrl, " +
                        "COUNT(s) AS followerCount " +
                        "FROM User u JOIN u.followers s " +
                        "WHERE u.isActive = true AND u.isBlocked = false " +
                        "GROUP BY u.id, u.username, u.profilePictureUrl " +
                        "ORDER BY COUNT(s) DESC, u.username ASC")
        List<PopularUserProjection> findPopularUsers(Pageable pageable);
}
