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
