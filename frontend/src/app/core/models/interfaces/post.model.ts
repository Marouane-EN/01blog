// ── Generic Wrapper ──────────────────────────────────────────────────
export interface CursorResponse<T> {
  readonly data: readonly T[];
  readonly nextCursor: number | null;
  readonly hasMore: boolean;
}

// ── User Models ──────────────────────────────────────────────────────
export interface UserPreview {
  readonly id: number;
  readonly username: string;
  readonly profileImageUrl: string | null;
  readonly profilePictureUrl?: string | null;
}

export interface Comment {
  readonly id: number;
  readonly content: string;
  readonly author: UserPreview;
  readonly replies: readonly Comment[];
  readonly likeCount: number;
  readonly likedByCurrentUser: boolean;
  readonly createdAt: string;
  readonly updatedAt: string;
}

export interface CreateCommentRequest {
  readonly content: string;
  readonly parentId?: number | null;
}

export interface CreatePostRequest {
  readonly title: string;
  readonly content: string;
  readonly tags: readonly string[];
}

export interface LikeResponse {
  readonly isLiked: boolean;
  readonly totalLikes: number;
}

export interface PostSearchResult {
  readonly author: string;
  readonly title: string;
  readonly createAt: string;
}

export interface Notification {
  readonly id: number;
  readonly senderUsername: string;
  readonly senderProfilePictureUrl: string | null;
  readonly type: string;
  readonly postId: number | null;
  readonly isRead: boolean;
  readonly createdAt: string;
}

export interface SubscriptionResponse {
  readonly isSubscribed: boolean;
  readonly totalSubscribers: number;
}

export interface PublicProfile {
  readonly id: number;
  readonly username: string;
  readonly bio: string | null;
  readonly profilePictureUrl: string | null;
  readonly postsCount: number;
  readonly followersCount: number;
  readonly followingCount: number;
  readonly joinedAt: string;
}

// ── Post Models ──────────────────────────────────────────────────────
export interface Post {
  readonly id: number;
  readonly slug: string;
  readonly title: string;
  readonly content: string;
  readonly author: UserPreview;
  readonly tag: readonly string[]; // Backend sends an array of strings
  readonly mediaUrls: readonly string[];
  readonly totalLikes: number; // Updated from likesCount
  readonly totalComments: number; // Updated from commentsCount
  readonly likedByCurrentUser: boolean; // Updated from isLikedByMe
  readonly createAt: string; // ISO-8601 string
  readonly updatedAt: string;
}

// Feed Tab Types
export type FeedTab = 'latest' | 'following';
