package _Blog_Backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import _Blog_Backend.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
}