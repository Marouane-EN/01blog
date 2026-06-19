package _Blog_Backend.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import org.hibernate.annotations.SQLRestriction;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_profile_pic", columnList = "profile_picture_url")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("is_Active = true AND is_Blocked = false")
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

    public void addSubscription(Subscription subscription) {
        this.following.add(subscription);
        subscription.getTargetUser().getFollowers().add(subscription);
    }

    public void removeSubscription(Subscription subscription) {
        this.following.remove(subscription);
        subscription.getTargetUser().getFollowers().remove(subscription);
    }
}