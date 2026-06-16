package _Blog_Backend.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import _Blog_Backend.dto.CommentDto;
import _Blog_Backend.dto.CommentRequest;
import _Blog_Backend.dto.CursorResponse;
import _Blog_Backend.dto.UserProfileDTO;
import _Blog_Backend.entity.Comment;
import _Blog_Backend.entity.Post;
import _Blog_Backend.entity.User;
import _Blog_Backend.repository.CommentRepository;
import _Blog_Backend.repository.PostRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public CommentDto createComment(CommentRequest request, Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        Comment comment = Comment.builder().content(request.content()).post(post).author(user).build();
        if (request.parentId() != null) {
            Comment parentComment = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent comment not found"));
            if (!parentComment.getPost().getId().equals(postId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Parent comment does not belong to the same post");
            }
            comment.setParent(parentComment);
        }

        commentRepository.save(comment);
        return mapToDto(comment);
    }

    @Transactional(readOnly = true)
    public CursorResponse<CommentDto> getCommentsForPost(Long postId, Long cursor) {
        if (!postRepository.existsById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }
        List<Comment> comments;
        Pageable pageRequest = PageRequest.of(0, 10);

        if (cursor == null) {
            comments = commentRepository.findByPostIdAndParentCommentIsNullOrderByIdDesc(postId, pageRequest);
        } else {
            comments = commentRepository.findByPostIdAndParentCommentIsNullAndIdLessThanOrderByIdDesc(postId, cursor,
                    pageRequest);
        }

        Long nextCursor = null;
        boolean hasMore = false;

        if (!comments.isEmpty()) {
            nextCursor = comments.get(comments.size() - 1).getId();
            hasMore = 10 == comments.size();
        }

        List<CommentDto> cleanComments = comments.stream().map(this::mapToDto).toList();
        return new CursorResponse<>(cleanComments, nextCursor, hasMore);
    }

    public CommentDto updateComment(Long commentId, String content, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to edit this comment.");
        }
        if (comment.getPost().isHidden()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "This discussion is locked. Comments cannot be edited.");
        }

        comment.setContent(content);
        Comment savedComment = commentRepository.save(comment);

        return mapToDto(savedComment);
    }

    @Transactional
    public void deleteComment(Long commentId, User currentUser) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own comments.");
        }

        if (comment.getPost().isHidden()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "This discussion is locked. Comments cannot be edited.");
        }

        if (comment.getReplies().isEmpty()) {
            commentRepository.delete(comment);
        } else {
            comment.setContent("[This comment has been deleted]");

            commentRepository.save(comment);
        }
    }

    private CommentDto mapToDto(Comment comment) {
        UserProfileDTO authorDto = new UserProfileDTO(
                comment.getAuthor().getId(),
                comment.getAuthor().getUsername(),
                comment.getAuthor().getProfilePictureUrl());

        List<CommentDto> replyDtos = comment.getReplies().stream()
                .map(this::mapToDto)
                .toList();

        return new CommentDto(
                comment.getId(),
                comment.getContent(),
                authorDto,
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                replyDtos);
    }

}
