package _Blog_Backend.service;

import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import _Blog_Backend.dto.UserDto;
import _Blog_Backend.dto.CommentDto;
import _Blog_Backend.dto.CommentRequest;
import _Blog_Backend.dto.CursorResponse;
import _Blog_Backend.entity.Comment;
import _Blog_Backend.entity.Post;
import _Blog_Backend.entity.User;
import _Blog_Backend.repository.CommentRepository;
import _Blog_Backend.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Transactional
    public CommentDto createComment(CommentRequest request, Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        Comment comment = Comment.builder().content(request.content()).post(post).author(user).build();
        if (request.parentId() != null) {
            Comment parentComment = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent comment not found"));
            if (parentComment.isDeleted()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot reply to a deleted comment.");
            }
            if (!parentComment.getPost().getId().equals(postId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Parent comment does not belong to the same post");
            }
            comment.setParent(parentComment);
        }

        commentRepository.save(comment);
        return mapToDto(comment, user.getId());
    }

    @Transactional(readOnly = true)
    public CursorResponse<CommentDto> getCommentsForPost(Long postId, Long cursor, User currentUser) {
        if (!postRepository.existsById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }
        List<Comment> comments;
        Pageable pageRequest = PageRequest.of(0, 11);

        if (cursor == null) {
            comments = commentRepository.findByPostIdAndParentIsNullOrderByIdDesc(postId, pageRequest);
        } else {
            comments = commentRepository.findByPostIdAndParentIsNullAndIdLessThanOrderByIdDesc(postId, cursor,
                    pageRequest);
        }

        Long nextCursor = null;
        boolean hasMore = comments.size() > 10;

        if (hasMore) {
            comments.remove(comments.size() - 1);
            nextCursor = comments.get(comments.size() - 1).getId();
        }

        List<CommentDto> cleanComments = comments.stream().map(comment -> mapToDto(comment, currentUser.getId()))
                .toList();
        return new CursorResponse<>(cleanComments, nextCursor, hasMore);
    }

    @Transactional
    public CommentDto updateComment(Long commentId, String content, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to edit this comment.");
        }

        if (comment.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can not edit a deleted comment");
        }

        try {
            comment.getPost();
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "This discussion is locked. Comments cannot be edited.");
        }

        comment.setContent(content);
        Comment savedComment = commentRepository.save(comment);

        return mapToDto(savedComment, currentUser.getId());
    }

    @Transactional
    public void deleteComment(Long commentId, User currentUser) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own comments.");
        }

        if (comment.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The comment is already deleted");
        }

        try {
            Hibernate.initialize(comment.getPost());

        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "This discussion is locked. Comments cannot be edited.");
        }

        if (comment.getReplies().isEmpty()) {
            commentRepository.delete(comment);
        } else {
            comment.setDeleted(true);
            comment.setContent("[This comment has been deleted]");
            commentRepository.save(comment);
        }
    }

    private CommentDto mapToDto(Comment comment, Long currentUserId) {

        UserDto authorDto;
        if (comment.isDeleted()) {
            authorDto = new UserDto(0L, "", null);
        } else {
            authorDto = new UserDto(
                    comment.getAuthor().getId(),
                    comment.getAuthor().getUsername(),
                    comment.getAuthor().getProfilePictureUrl());
        }

        List<CommentDto> replyDtos = comment.getReplies().stream()
                .map(reply -> mapToDto(reply, currentUserId))
                .toList();

        int totalLikes = comment.getLikes() != null ? comment.getLikes().size() : 0;

        boolean likedByCurrentUser = false;
        if (comment.getLikes() != null) {
            likedByCurrentUser = comment.getLikes().stream()
                    .anyMatch(like -> like.getUser().getId().equals(currentUserId));
        }

        return new CommentDto(
                comment.getId(),
                comment.getContent(),
                authorDto,
                replyDtos,
                totalLikes,
                likedByCurrentUser,
                comment.getCreatedAt(),
                comment.getUpdatedAt());
    }

}
