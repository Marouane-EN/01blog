package _Blog_Backend.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import _Blog_Backend.dto.CursorResponse;
import _Blog_Backend.dto.PostDto;
import _Blog_Backend.dto.PostRequest;
import _Blog_Backend.dto.UserProfileDTO;
import _Blog_Backend.entity.Post;
import _Blog_Backend.entity.PostMedia;
import _Blog_Backend.entity.User;
import _Blog_Backend.repository.PostRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final LocalFileStorageService fileStorageService;

    public PostDto createPost(PostRequest request, List<MultipartFile> images, User author) {
        String slag = generateUniqueSlug(request.title());
        String tag = null;
        if (request.tag() != null && !request.tag().isBlank()) {
            tag = request.tag().trim();
        }

        Post newPost = Post.builder().title(request.title()).description(request.content()).tag(tag)
                .author(author).slug(slag).build();

        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                if (!file.isEmpty()) {
                    try {
                        String mediaUrl = fileStorageService.savePostMedia(file);

                        PostMedia postMedia = PostMedia.builder()
                                .post(newPost)
                                .mediaUrl(mediaUrl)
                                .build();

                        newPost.getMediaList().add(postMedia);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to process image: " + file.getOriginalFilename(), e);
                    }
                }
            }
        }
        Post savedPost = postRepository.save(newPost);

        return mapToDto(savedPost);
    }

    public PostDto getPostBySlug(String slug) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found"));
        return mapToDto(post);
    }

    public CursorResponse<PostDto> getPostFeed(Long cursor) {
        List<Post> posts;
        Pageable pageRequest = PageRequest.of(0, 10);
        if (cursor == null) {
            posts = postRepository.findAllByOrderByIdDesc(pageRequest);
        } else {
            posts = postRepository.findByIdLessThanOrderByIdDesc(cursor, pageRequest);
        }
        Long nextCursor = null;
        boolean hasMore = false;

        if (!posts.isEmpty()) {
            nextCursor = posts.get(posts.size() - 1).getId();

            hasMore = posts.size() == 10;
        }
        List<PostDto> cleanPosts = posts.stream().map(this::mapToDto).toList();
        return new CursorResponse<>(cleanPosts, nextCursor, hasMore);
    }

    private PostDto mapToDto(Post post) {
        UserProfileDTO auther = new UserProfileDTO(post.getAuthor().getId(), post.getAuthor().getUsername(),
                post.getAuthor().getProfilePictureUrl());

        List<String> mediaUrls = post.getMediaList().stream()
                .map(PostMedia::getMediaUrl)
                .toList();

        return new PostDto(post.getId(), auther, post.getTitle(), post.getDescription(), post.getTag(),
                mediaUrls, post.getCreatedAt());
    }

    private static String generateUniqueSlug(String title) {
        String baseSlug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        String uniqueHash = UUID.randomUUID().toString().substring(0, 6);
        String suffix = "-" + uniqueHash;

        int maxSlugLength = 128;
        int maxBaseLength = maxSlugLength - suffix.length();

        if (baseSlug.length() > maxBaseLength) {
            baseSlug = baseSlug.substring(0, maxBaseLength);

            if (baseSlug.endsWith("-")) {
                baseSlug = baseSlug.substring(0, baseSlug.length() - 1);
            }
        }

        return baseSlug + suffix;
    }
}
