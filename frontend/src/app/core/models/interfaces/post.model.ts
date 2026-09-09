import { UserDto } from './user.model';

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
  readonly id: number;
  readonly author: UserDto;
  readonly title: string;
  readonly slug: string;
  readonly createAt: string;
}

// ── Post Models ──────────────────────────────────────────────────────
export interface PostMedia {
  readonly id: number;
  readonly url: string;
}

export interface Post {
  readonly id: number;
  readonly slug: string;
  readonly title: string;
  readonly content: string;
  readonly author: UserDto;
  readonly tag: readonly string[]; // Backend sends an array of strings
  readonly media: readonly PostMedia[];
  readonly totalLikes: number; // Updated from likesCount
  readonly totalComments: number; // Updated from commentsCount
  readonly likedByCurrentUser: boolean; // Updated from isLikedByMe
  readonly createAt: string; // ISO-8601 string
  readonly updatedAt: string;
}

// Feed Tab Types
export type FeedTab = 'latest' | 'following';
