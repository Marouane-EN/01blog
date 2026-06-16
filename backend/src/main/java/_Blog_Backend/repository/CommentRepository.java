package _Blog_Backend.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import _Blog_Backend.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @EntityGraph(attributePaths = { "author" })
    List<Comment> findByPostIdAndParentCommentIsNullOrderByIdDesc(Long postId, Pageable pageable);

    @EntityGraph(attributePaths = { "author" })
    List<Comment> findByPostIdAndParentCommentIsNullAndIdLessThanOrderByIdDesc(Long postId, Long id, Pageable pageable);
}
