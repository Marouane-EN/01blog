import { UserPreview } from './user.model';

export interface Tag {
  readonly id: string;
  readonly name: string;
  readonly color: string | null; // optional hex like '#3b82f6'
}

export interface Post {
  readonly id: string;
  readonly slug: string;
  readonly title: string;
  readonly excerpt: string | null;
  readonly coverImageUrl: string | null;
  readonly author: UserPreview;
  readonly tags: readonly Tag[];
  readonly likesCount: number;
  readonly commentsCount: number;
  readonly readingTimeMinutes: number;
  readonly publishedAt: string; // ISO 8601
  readonly isLikedByMe: boolean;
  readonly isBookmarkedByMe: boolean;
}

export interface PostFeed {
  readonly posts: readonly Post[];
  readonly nextCursor: string | null;
  readonly hasMore: boolean;
}

export type FeedTab = 'for-you' | 'following' | 'latest';
