package _Blog_Backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import _Blog_Backend.dto.TagTrendProjection;
import _Blog_Backend.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);

    // Most-used tags across visible posts, ranked by how many posts carry them.
    @Query("SELECT t.id AS id, t.name AS name, COUNT(p) AS postCount " +
            "FROM Post p JOIN p.tags t " +
            "WHERE p.isHidden = false " +
            "GROUP BY t.id, t.name " +
            "ORDER BY COUNT(p) DESC, t.name ASC")
    List<TagTrendProjection> findTrendingTags(Pageable pageable);
}
