package _Blog_Backend.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import _Blog_Backend.dto.AuthorDto;
import _Blog_Backend.dto.CursorResponse;
import _Blog_Backend.dto.ImageUploadResponse;
import _Blog_Backend.dto.PostDto;
import _Blog_Backend.dto.PostRequest;
import _Blog_Backend.entity.Post;
import _Blog_Backend.entity.PostMedia;
import _Blog_Backend.entity.Tag;
import _Blog_Backend.entity.User;
import _Blog_Backend.repository.LikeRepository;
import _Blog_Backend.repository.PostRepository;
import _Blog_Backend.repository.TagRepository;
import _Blog_Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final TagRepository tagRepository;
    private final LocalFileStorageService fileStorageService;
    private final NotificationService notificationService;

    @Transactional
    public PostDto createPost(PostRequest request, List<MultipartFile> images, User author) {
        String slug = generateUniqueSlug(request.title());

        Post newPost = Post.builder().title(request.title()).description(request.content())
                .author(author).slug(slug).build();
        newPost.setTags(stringsToTags(request.tags()));

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
        notificationService.notifyFollowersOfNewPost(author, savedPost);
        List<Long> likedPostIds = likeRepository.findLikedPostIdsByUser(author.getId(), List.of(savedPost.getId()));
        return mapToDto(savedPost, likedPostIds);
    }

    @Transactional(readOnly = true)
    public PostDto getPostBySlug(String slug, User currentUser) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found"));

        if (post.getAuthor().isBlocked() || !post.getAuthor().isActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This article is no longer available.");
        }
        List<Long> likedPostIds = likeRepository.findLikedPostIdsByUser(currentUser.getId(), List.of(post.getId()));
        return mapToDto(post, likedPostIds);
    }

    @Transactional
    public PostDto updatePost(Long postId, PostRequest request, User currentUser) {

        Post existingPost = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (!existingPost.getAuthor().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to edit this post.");
        }

        existingPost.setTitle(request.title());
        existingPost.setDescription(request.content());
        Set<String> newTags = Set.of(request.tags()).stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(tag -> tag.toLowerCase().trim())
                .collect(Collectors.toSet());

        Set<String> existingTags = existingPost.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        if (!existingTags.equals(newTags)) {
            List<Tag> updatedTags = stringsToTags(request.tags());
            existingPost.getTags().clear();
            existingPost.getTags().addAll(updatedTags);

            existingPost.setUpdatedAt(LocalDateTime.now());
        }

        Post savedPost = postRepository.save(existingPost);

        List<Long> likedPostIds = likeRepository.findLikedPostIdsByUser(currentUser.getId(),
                List.of(savedPost.getId()));

        return mapToDto(savedPost, likedPostIds);
    }

    @Transactional(readOnly = true)
    public CursorResponse<PostDto> getPostFeed(Long cursor, User currentUser) {
        List<Post> posts;

        Pageable pageRequest = PageRequest.of(0, 11);

        if (cursor == null) {
            posts = postRepository.findPublicFeed(pageRequest);
        } else {
            posts = postRepository.findPublicFeedByCursor(cursor, pageRequest);
        }

        Long nextCursor = null;
        boolean hasMore = posts.size() > 10;

        if (hasMore) {
            posts.remove(posts.size() - 1);
            nextCursor = posts.get(posts.size() - 1).getId();
        }

        List<Long> postIds = posts.stream().map(Post::getId).toList();

        List<Long> likedPostIds = likeRepository.findLikedPostIdsByUser(currentUser.getId(), postIds);

        List<PostDto> cleanPosts = posts.stream()
                .map(post -> this.mapToDto(post, likedPostIds))
                .toList();

        return new CursorResponse<>(cleanPosts, nextCursor, hasMore);
    }

    @Transactional(readOnly = true)
    public CursorResponse<PostDto> getPostsByUser(Long authorId, Long cursor, Long currentUserId) {

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (author.isBlocked() || !author.isActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User unavailable");
        }

        Pageable pageRequest = PageRequest.of(0, 11);
        List<Post> posts;

        if (cursor == null) {
            posts = postRepository.findByAuthorId(authorId, pageRequest);
        } else {
            posts = postRepository.findByAuthorIdAndCursor(authorId, cursor, pageRequest);
        }

        boolean hasMore = posts.size() > 10;
        Long nextCursor = null;

        if (hasMore) {
            posts.remove(posts.size() - 1);
            nextCursor = posts.get(posts.size() - 1).getId();
        }

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        List<Long> likedPostIds = likeRepository.findLikedPostIdsByUser(currentUserId, postIds);

        List<PostDto> cleanPosts = posts.stream()
                .map(post -> mapToDto(post, likedPostIds))
                .toList();

        return new CursorResponse<>(cleanPosts, nextCursor, hasMore);
    }

    @Transactional(readOnly = true)
    public CursorResponse<PostDto> getSubscriptionsFeed(Long cursor, User currentUser) {

        Pageable pageRequest = PageRequest.of(0, 11);
        List<Post> posts;

        if (cursor == null) {
            posts = postRepository.findSubscriptionsFeed(currentUser.getId(), pageRequest);
        } else {
            posts = postRepository.findSubscriptionsFeedByCursor(currentUser.getId(), cursor, pageRequest);
        }

        boolean hasMore = posts.size() > 10;
        Long nextCursor = null;

        if (hasMore) {
            posts.remove(posts.size() - 1);
            nextCursor = posts.get(posts.size() - 1).getId();
        }

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        List<Long> likedPostIds = likeRepository.findLikedPostIdsByUser(currentUser.getId(), postIds);

        List<PostDto> cleanPosts = posts.stream()
                .map(post -> mapToDto(post, likedPostIds))
                .toList();

        return new CursorResponse<>(cleanPosts, nextCursor, hasMore);
    }

    @Transactional(readOnly = true)
    public CursorResponse<PostDto> searchPosts(String keyword, Long cursor, User currentUser) {

        Pageable pageRequest = PageRequest.of(0, 11);
        List<Post> posts;

        if (cursor == null) {
            posts = postRepository.searchPublicPosts(keyword, pageRequest);
        } else {
            posts = postRepository.searchPublicPostsByCursor(keyword, cursor, pageRequest);
        }

        boolean hasMore = posts.size() > 10;
        Long nextCursor = null;

        if (hasMore) {
            posts.remove(posts.size() - 1);
            nextCursor = posts.get(posts.size() - 1).getId();
        }

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        List<Long> likedPostIds = likeRepository.findLikedPostIdsByUser(currentUser.getId(), postIds);

        List<PostDto> cleanPosts = posts.stream()
                .map(post -> mapToDto(post, likedPostIds))
                .toList();

        return new CursorResponse<>(cleanPosts, nextCursor, hasMore);
    }

    @Transactional
    public void deletePost(Long postId, User currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        boolean isAuthor = post.getAuthor().getId().equals(currentUser.getId());

        if (!isAuthor) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to delete this post.");
        }
        post.setHidden(true);
        postRepository.save(post);
    }

    @Transactional
    public ImageUploadResponse addImageToPost(Long postId, MultipartFile file, User currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to edit this post.");
        }

        try {
            String imageUrl = fileStorageService.savePostMedia(file);

            PostMedia postMedia = new PostMedia();
            postMedia.setMediaUrl(imageUrl);
            post.addMedia(postMedia);
            postRepository.save(post);

            return new ImageUploadResponse(postMedia.getId(), imageUrl);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process image: " + file.getOriginalFilename(), e);
        }
    }

    @Transactional
    public void deleteImageFromPost(Long postId, Long imageId, User currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to edit this post.");
        }
        PostMedia mediaToRemove = post.getMediaList().stream()
                .filter(media -> media.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found in this post"));

        post.getMediaList().remove(mediaToRemove);
        fileStorageService.deleteFile("/" + mediaToRemove.getMediaUrl());
        postRepository.save(post);
    }

    private List<Tag> stringsToTags(String[] tagStrings) {
        if (tagStrings == null || tagStrings.length == 0) {
            return List.of();
        }
        return List.of(tagStrings).stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(tag -> tag.toLowerCase().trim())
                .map(tag -> {
                    if (tag.length() > 30) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Tag must be between 1 and 30 characters");
                    }
                    return tagRepository.findByName(tag)
                            .orElseGet(() -> {
                                Tag newTag = Tag.builder().name(tag).build();
                                return tagRepository.save(newTag);
                            });
                })
                .toList();
    }

    private PostDto mapToDto(Post post, List<Long> likedPostIds) {

        AuthorDto authorDto = (post.getAuthor().isBlocked() || !post.getAuthor().isActive())
                ? new AuthorDto(0L, "[Suspended Account]", null)
                : new AuthorDto(post.getAuthor().getId(), post.getAuthor().getUsername(),
                        post.getAuthor().getProfilePictureUrl());

        List<String> mediaUrls = post.getMediaList().stream()
                .map(PostMedia::getMediaUrl)
                .toList();
        List<String> tags = post.getTags().stream()
                .map(Tag::getName)
                .toList();
        boolean likedByCurrentUser = likedPostIds.contains(post.getId());

        return new PostDto(
                post.getId(),
                post.getSlug(),
                authorDto,
                post.getTitle(),
                post.getDescription(),
                tags,
                mediaUrls,
                post.getLikesCount(),
                post.getCommentsCount(),
                likedByCurrentUser,
                post.getCreatedAt(),
                post.getUpdatedAt());
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
