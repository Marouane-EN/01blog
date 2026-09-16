package _Blog_Backend.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.annotations.Formula;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_profile_pic", columnList = "profile_picture_url")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(nullable = false, length = 5)
    private String role = "USER";

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    private String profilePictureUrl;

    private LocalDate birthDate;

    @Column(length = 500)
    private String bio;

    @Column(nullable = true)
    private String passwordHash;

    @Builder.Default
    @Column(nullable = false, length = 10)
    private String authProvider = "LOCAL";

    @Column(nullable = true)
    private String providerId;

    @OneToMany(mappedBy = "targetUser", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Subscription> followers = new ArrayList<>();

    @OneToMany(mappedBy = "subscriber", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Subscription> following = new ArrayList<>();

    @Formula("(SELECT COUNT(*) FROM posts p WHERE p.user_id = id AND p.is_hidden = false)")
    private int postsCount;

    @Formula("(SELECT COUNT(*) FROM subscriptions s WHERE s.target_user_id = id)")
    private int followersCount;

    @Formula("(SELECT COUNT(*) FROM subscriptions s WHERE s.subscriber_id = id)")
    private int followingCount;

    @Builder.Default
    @Column(nullable = false)
    private boolean isActive = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean isBlocked = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isBlocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    public int getPostsCount() {
        return postsCount;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void addSubscription(Subscription subscription) {
        this.following.add(subscription);
        subscription.getTargetUser().getFollowers().add(subscription);
    }

    public void removeSubscription(Subscription subscription) {
        this.following.remove(subscription);
        subscription.getTargetUser().getFollowers().remove(subscription);
    }
}