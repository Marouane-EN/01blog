package _Blog_Backend.service;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import _Blog_Backend.dto.LikeResponse;
import _Blog_Backend.entity.Like;
import _Blog_Backend.entity.Post;
import _Blog_Backend.entity.User;
import _Blog_Backend.repository.LikeRepository;
import _Blog_Backend.repository.PostRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;

    @Transactional
    public LikeResponse togglePostLike(Long postId, User currentUser) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (post.isHidden()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot like a hidden post.");
        }

        Optional<Like> existingLike = likeRepository.findByPostIdAndUserId(postId, currentUser.getId());

        boolean isNowLiked;

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            isNowLiked = false;
        } else {
            Like newLike = Like.builder()
                    .post(post)
                    .user(currentUser)
                    .build();
            likeRepository.save(newLike);
            isNowLiked = true;
        }

        long newTotalLikes = likeRepository.countByPostId(postId);

        return new LikeResponse(isNowLiked, newTotalLikes);
    }
}
