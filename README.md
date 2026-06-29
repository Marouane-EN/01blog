## API Reference

Base URL: `http://localhost:8080`

Authentication:

- Public routes: `/api/auth/**`, `/oauth2/**`, `/error`
- All other routes require `Authorization: Bearer <jwt>`
- Admin routes under `/api/admin/**` require an admin user
- Rate-limited routes may return `429` with the text body `Too many attempts. Please try again in 15 minutes.`

Dates are returned as ISO-8601 strings, for example `2026-06-29T10:15:30`.

### Auth Endpoints

| Method | Endpoint | Request DTO | Response DTO |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | `RegisterRequest` | `201 AuthResponse` |
| `POST` | `/api/auth/login` | `LoginRequest` | `200 AuthResponse` |

### Post Endpoints

| Method | Endpoint | Request DTO | Response DTO |
| --- | --- | --- | --- |
| `POST` | `/api/posts` | Multipart form: `postData: PostRequest`, optional `files: File[]` | `201 PostDto` |
| `GET` | `/api/posts?cursor={postId}` | None | `CursorResponse<PostDto>` |
| `GET` | `/api/posts/{slug}` | None | `201 PostDto` |
| `GET` | `/api/posts/user/{authorId}?cursor={postId}` | None | `CursorResponse<PostDto>` |
| `GET` | `/api/posts/subscriptions?cursor={postId}` | None | `CursorResponse<PostDto>` |
| `GET` | `/api/posts/search?q={keyword}&cursor={postId}` | None | `CursorResponse<PostSearchDto>` |
| `PUT` | `/api/posts/{id}` | `PostRequest` | `PostDto` |
| `DELETE` | `/api/posts/{id}` | None | Text: `Post deleted successfully` |
| `POST` | `/api/posts/{postId}/files` | Multipart form: `file: File` | `201 FileUploadResponse` |
| `DELETE` | `/api/posts/{postId}/files/{mediaId}` | None | Text: `Media deleted successfully` |
| `POST` | `/api/posts/{postId}/like` | None | `LikeResponse` |

### Comment Endpoints

| Method | Endpoint | Request DTO | Response DTO |
| --- | --- | --- | --- |
| `POST` | `/api/posts/{postId}/comments` | `CommentRequest` | `201 CommentDto` |
| `GET` | `/api/posts/{postId}/comments?cursor={commentId}` | None | `CursorResponse<CommentDto>` |
| `PUT` | `/api/posts/{postId}/comments/{commentId}` | Raw string content | `CommentDto` |
| `DELETE` | `/api/posts/{postId}/comments/{commentId}` | None | Text: `Comment deleted successfully` |
| `POST` | `/api/posts/{postId}/comments/{commentId}/likes` | None | `LikeResponse` |

### User Endpoints

| Method | Endpoint | Request DTO | Response DTO |
| --- | --- | --- | --- |
| `GET` | `/api/users/me` | None | `UserDto` |
| `POST` | `/api/users/me/avatar` | Multipart form: `file: File` | Text: avatar URL |
| `POST` | `/api/users/{username}/subscribe` | None | `SubscriptionResponse` |
| `GET` | `/api/users/{userId}/followers?cursor={subscriptionId}` | None | `CursorResponse<UserDto>` |
| `GET` | `/api/users/{userId}/following?cursor={subscriptionId}` | None | `CursorResponse<UserDto>` |
| `GET` | `/api/users/{username}` | None | `PublicProfileDto` |
| `GET` | `/api/users/search?q={keyword}&cursor={userId}` | None | `CursorResponse<UserDto>` |

### Notification Endpoints

| Method | Endpoint | Request DTO | Response DTO |
| --- | --- | --- | --- |
| `GET` | `/api/notifications?cursor={notificationId}` | None | `CursorResponse<NotificationDto>` |
| `GET` | `/api/notifications/unreadcount` | None | Number |
| `PUT` | `/api/notifications/{id}/readOrUnread` | None | Text message |

### Report Endpoints

| Method | Endpoint | Request DTO | Response DTO |
| --- | --- | --- | --- |
| `POST` | `/api/posts/{postId}/reports` | `ReportRequest` | Text: `Post reported successfully.` |
| `POST` | `/api/comments/{commentId}/reports` | `ReportRequest` | Text: `Comment reported successfully.` |
| `POST` | `/api/users/{userId}/reports` | `ReportRequest` | Text: `Profile reported successfully.` |

### Admin Endpoints

| Method | Endpoint | Request DTO | Response DTO |
| --- | --- | --- | --- |
| `GET` | `/api/admin/reports?page={page}&size={size}` | None | `Page<AdminReportDto>` |
| `PUT` | `/api/admin/reports/{reportId}/resolve` | `ResolveRequest` | Text: `Report resolved successfully.` |
| `GET` | `/api/admin/users?search={keyword}&page={page}&size={size}` | None | `Page<AdminUserDto>` |
| `GET` | `/api/admin/users/{userId}?page={page}&size={size}` | None | `Page<AdminPostDto>` |
| `GET` | `/api/admin/posts?search={keyword}&page={page}&size={size}` | None | `Page<AdminPostDto>` |
| `GET` | `/api/admin/posts/{postId}` | None | `AdminPostDetailsDto` |
| `GET` | `/api/admin/posts/{postId}/comments?page={page}&size={size}` | None | `Page<AdminCommentDto>` |
| `PUT` | `/api/admin/users/{userId}/ban` | None | `MessageResponse` |
| `DELETE` | `/api/admin/users/{userId}` | None | `MessageResponse` |
| `PUT` | `/api/admin/posts/{postId}/hide` | None | `MessageResponse` |
| `DELETE` | `/api/admin/posts/{postId}` | None | `MessageResponse` |
| `DELETE` | `/api/admin/comments/{commentId}` | None | Text: `Comment has been scrubbed.` |

### Request DTOs

#### RegisterRequest

```json
{
  "username": "student01",
  "email": "student@example.com",
  "password": "password123",
  "bio": "Learning Java and Angular.",
  "birthDate": "2000-01-31"
}
```

Validation:

- `username`: required, 3-20 characters
- `email`: required, valid email
- `password`: required, minimum 8 characters
- `bio`: optional, maximum 500 characters
- `birthDate`: optional, must be in the past

#### LoginRequest

```json
{
  "identifier": "student01",
  "password": "password123"
}
```

Validation:

- `identifier`: required, 3-50 characters; can be username or email
- `password`: required, minimum 8 characters

#### PostRequest

```json
{
  "title": "My first post",
  "content": "Today I learned how Spring controllers work.",
  "tags": ["spring", "angular"]
}
```

Validation:

- `title`: required, 1-128 characters
- `content`: required, 1-1000 characters
- `tags`: optional, maximum 5 tags; each saved tag must be 1-30 characters

For `POST /api/posts`, send this DTO as the multipart part named `postData`.

#### CommentRequest

```json
{
  "content": "Great explanation!",
  "parentId": null
}
```

Validation:

- `content`: required, maximum 200 characters
- `parentId`: optional; set it to reply to another comment

#### ReportRequest

```json
{
  "reason": "This content contains inappropriate language."
}
```

Validation:

- `reason`: required, 10-500 characters

#### ResolveRequest

```json
{
  "action": "BAN_USER"
}
```

Allowed `action` values:

- `BAN_USER`
- `HIDE_CONTENT`
- `DISMISS`

### Response DTOs

#### AuthResponse

```json
{
  "token": "jwt-token",
  "userProfile": {
    "id": 1,
    "username": "student01",
    "profileImageUrl": "https://example.com/avatar.png"
  }
}
```

#### UserDto

```json
{
  "id": 1,
  "username": "student01",
  "profileImageUrl": "https://example.com/avatar.png"
}
```

#### PublicProfileDto

```json
{
  "id": 1,
  "username": "student01",
  "bio": "Learning Java and Angular.",
  "profilePictureUrl": "https://example.com/avatar.png",
  "postsCount": 7,
  "followersCount": 25,
  "followingCount": 10,
  "joinedAt": "2026-06-29T10:15:30"
}
```

#### PostDto

```json
{
  "id": 10,
  "slug": "my-first-post-a1b2c3",
  "author": {
    "id": 1,
    "username": "student01",
    "profileImageUrl": "https://example.com/avatar.png"
  },
  "title": "My first post",
  "content": "Today I learned how Spring controllers work.",
  "tag": ["spring", "angular"],
  "mediaUrls": ["https://example.com/media.png"],
  "totalLikes": 4,
  "totalComments": 2,
  "likedByCurrentUser": true,
  "createAt": "2026-06-29T10:15:30",
  "updatedAt": "2026-06-29T11:00:00"
}
```

#### PostSearchDto

```json
{
  "author": "student01",
  "title": "My first post",
  "createAt": "2026-06-29T10:15:30"
}
```

#### CommentDto

```json
{
  "id": 50,
  "content": "Great explanation!",
  "author": {
    "id": 2,
    "username": "reader01",
    "profileImageUrl": "https://example.com/avatar.png"
  },
  "replies": [],
  "likeCount": 3,
  "likedByCurrentUser": false,
  "createdAt": "2026-06-29T10:30:00",
  "updatedAt": "2026-06-29T10:45:00"
}
```

#### CursorResponse<T>

```json
{
  "data": [],
  "nextCursor": 42,
  "hasMore": true
}
```

`data` contains the DTO named by the endpoint, such as `PostDto`, `CommentDto`, `UserDto`, `NotificationDto`, or `PostSearchDto`.

#### LikeResponse

```json
{
  "isLiked": true,
  "totalLikes": 5
}
```

#### FileUploadResponse

```json
{
  "mediaId": 99,
  "url": "https://example.com/media.png"
}
```

#### SubscriptionResponse

```json
{
  "isSubscribed": true,
  "totalSubscribers": 26
}
```

#### NotificationDto

```json
{
  "id": 15,
  "senderUsername": "student01",
  "senderProfilePictureUrl": "https://example.com/avatar.png",
  "type": "NEW_POST",
  "postId": 10,
  "isRead": false,
  "createdAt": "2026-06-29T10:15:30"
}
```

Allowed `type` values:

- `FOLLOW`
- `NEW_POST`

#### AdminReportDto

```json
{
  "reportId": 1,
  "reporterUsername": "reader01",
  "reportedUsername": "student01",
  "reportType": "POST",
  "targetId": 10,
  "reason": "This content contains inappropriate language.",
  "createdAt": "2026-06-29T10:15:30"
}
```

Allowed `reportType` values:

- `USER`
- `POST`
- `COMMENT`

#### AdminUserDto

```json
{
  "id": 1,
  "username": "student01",
  "email": "student@example.com",
  "isBlocked": false,
  "isActive": true,
  "createdAt": "2026-06-29T10:15:30"
}
```

#### AdminPostDto

```json
{
  "id": 10,
  "title": "My first post",
  "authorUsername": "student01",
  "authorProfilePictureUrl": "https://example.com/avatar.png",
  "isHidden": false,
  "likesCount": 4,
  "commentsCount": 2,
  "createdAt": "2026-06-29T10:15:30"
}
```

#### AdminPostDetailsDto

```json
{
  "id": 10,
  "title": "My first post",
  "description": "Today I learned how Spring controllers work.",
  "authorUsername": "student01",
  "authorProfilePictureUrl": "https://example.com/avatar.png",
  "isHidden": false,
  "tags": ["spring", "angular"],
  "mediaUrls": ["https://example.com/media.png"],
  "likesCount": 4,
  "createdAt": "2026-06-29T10:15:30"
}
```

#### AdminCommentDto

```json
{
  "id": 50,
  "authorUsername": "reader01",
  "authorProfilePictureUrl": "https://example.com/avatar.png",
  "content": "Great explanation!",
  "isDeleted": false,
  "createdAt": "2026-06-29T10:30:00"
}
```

#### Page<T>

Admin list endpoints return the default Spring `Page` wrapper:

```json
{
  "content": [],
  "pageable": {},
  "totalPages": 1,
  "totalElements": 1,
  "last": true,
  "size": 20,
  "number": 0,
  "sort": {},
  "numberOfElements": 1,
  "first": true,
  "empty": false
}
```

`content` contains the admin DTO named by the endpoint, such as `AdminReportDto`, `AdminUserDto`, `AdminPostDto`, or `AdminCommentDto`.

#### MessageResponse

Some admin toggle endpoints return a JSON message:

```json
{
  "message": "User has been banned."
}
```
