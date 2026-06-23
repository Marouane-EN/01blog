package _Blog_Backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    @Query(value = "SELECT * FROM users ORDER BY created_at DESC", 
           countQuery = "SELECT count(*) FROM users", 
           nativeQuery = true)
    Page<User> findAllForAdmin(Pageable pageable);

    Page<User> findByUsernameContainingIgnoreCase(String keyword, Pageable pageable);
}
