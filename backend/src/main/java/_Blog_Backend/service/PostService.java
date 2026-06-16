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
import _Blog_Backend.dto.ImageUploadResponse;
import _Blog_Backend.dto.PostDto;
import _Blog_Backend.dto.PostRequest;
import _Blog_Backend.dto.UserProfileDTO;
import _Blog_Backend.entity.Post;
import _Blog_Backend.entity.PostMedia;
import _Blog_Backend.entity.Tag;
import _Blog_Backend.entity.User;
import _Blog_Backend.repository.PostRepository;
import _Blog_Backend.repository.TagRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final LocalFileStorageService fileStorageService;

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

        return mapToDto(savedPost);
    }

    public PostDto getPostBySlug(String slug) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found"));
        return mapToDto(post);
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
        List<Tag> updatedTags = stringsToTags(request.tags());

        existingPost.getTags().clear();
        existingPost.getTags().addAll(updatedTags);

        Post savedPost = postRepository.save(existingPost);

        return mapToDto(savedPost);
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

    public void deletePost(Long postId, User currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        boolean isAuthor = post.getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole().equals("ADMIN");

        if (!(isAuthor || isAdmin)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to delete this post.");
        }
        post.setHidden(true);

        postRepository.save(post);
    }

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

    private PostDto mapToDto(Post post) {
        UserProfileDTO auther = new UserProfileDTO(post.getAuthor().getId(), post.getAuthor().getUsername(),
                post.getAuthor().getProfilePictureUrl());

        List<String> mediaUrls = post.getMediaList().stream()
                .map(PostMedia::getMediaUrl)
                .toList();
        List<String> tags = post.getTags().stream()
                .map(Tag::getName)
                .toList();

        return new PostDto(post.getId(), auther, post.getTitle(), post.getDescription(), tags,
                mediaUrls, post.getCreatedAt(), post.getUpdatedAt());
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
