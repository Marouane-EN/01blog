package _Blog_Backend.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import _Blog_Backend.dto.*;

import _Blog_Backend.entity.*;

import _Blog_Backend.repository.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final TagRepository tagRepository;
    private final FileUploadService fileUploadService;
    private final NotificationService notificationService;

    @Transactional
    public PostDto createPost(PostRequest request, List<MultipartFile> files, User author) {

        List<String> uploadedMediaUrls = fileUploadService.uploadMultipleFiles(files);
        String slug = generateUniqueSlug(request.title());

        Post newPost = Post.builder().title(request.title()).description(request.content())
                .author(author).slug(slug).build();
        newPost.setTags(stringsToTags(request.tags()));

        for (String url : uploadedMediaUrls) {
            PostMedia media = PostMedia.builder()
                    .mediaUrl(url)
                    .build();

            newPost.addMedia(media);
        }
        Post savedPost = postRepository.save(newPost);
        notificationService.notifyFollowersOfNewPost(author, savedPost);
        return mapToDto(savedPost, new ArrayList<>());
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
    public CursorResponse<PostSearchDto> searchPosts(String keyword, Long cursor, User currentUser) {

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

        List<PostSearchDto> cleanPosts = posts.stream()
                .map(post -> {
                    String author = (post.getAuthor().isBlocked() || !post.getAuthor().isActive())
                            ? "[Suspended Account]"
                            : post.getAuthor().getUsername();
                    return new PostSearchDto(author, post.getTitle(), post.getCreatedAt());
                })
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
    public FileUploadResponse addFileToPost(Long postId, MultipartFile file, User currentUser) throws IOException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to edit this post.");
        }

        String mediaUrl = fileUploadService.uploadFile(file);

        PostMedia postMedia = new PostMedia();
        postMedia.setMediaUrl(mediaUrl);
        post.addMedia(postMedia);
        postRepository.save(post);

        return new FileUploadResponse(postMedia.getId(), mediaUrl);
    }

    @Transactional
    public void deleteFileFromPost(Long postId, Long mediaId, User currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to edit this post.");
        }
        PostMedia mediaToRemove = post.getMediaList().stream()
                .filter(media -> media.getId().equals(mediaId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found in this post"));

        post.removeMedia(mediaToRemove);
        fileUploadService.deleteFileByUrl(mediaToRemove.getMediaUrl());
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

        UserDto UserDto = (post.getAuthor().isBlocked() || !post.getAuthor().isActive())
                ? new UserDto(0L, "[Suspended Account]", null)
                : new UserDto(post.getAuthor().getId(), post.getAuthor().getUsername(),
                        post.getAuthor().getProfilePictureUrl());

        List<String> mediaUrls = post.getMediaList().stream()
                .map(PostMedia::getMediaUrl)
                .toList();
        List<String> tags = post.getTags().stream()
                .map(Tag::getName)
                .toList();
        boolean likedByCurrentUser = false;
        if (!likedPostIds.isEmpty()) {
            likedByCurrentUser = likedPostIds.contains(post.getId());
        }

        return new PostDto(
                post.getId(),
                post.getSlug(),
                UserDto,
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
