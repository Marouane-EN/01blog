export type AdminAction = 'BAN_USER' | 'HIDE_CONTENT' | 'DISMISS';

export interface PageResponse<T> {
  readonly content: readonly T[];
  readonly totalElements: number;
  readonly totalPages: number;
  readonly number: number;
  readonly size: number;
  readonly first: boolean;
  readonly last: boolean;
}

export interface AdminReport {
  readonly reportId: number;
  readonly reporterUsername: string;
  readonly reportedUsername: string;
  readonly reportType: string;
  readonly targetId: number;
  readonly reason: string;
  readonly createdAt: string;
}

export interface AdminUser {
  readonly id: number;
  readonly username: string;
  readonly email: string;
  readonly role: string;
  readonly isBlocked: boolean;
  readonly isActive: boolean;
  readonly createdAt: string;
}

export interface AdminPost {
  readonly id: number;
  readonly title: string;
  readonly authorUsername: string;
  readonly authorProfilePictureUrl: string | null;
  readonly isHidden: boolean;
  readonly likesCount: number;
  readonly commentsCount: number;
  readonly createdAt: string;
}

export interface AdminPostDetails {
  readonly id: number;
  readonly title: string;
  readonly description: string;
  readonly authorUsername: string;
  readonly authorProfilePictureUrl: string | null;
  readonly isHidden: boolean;
  readonly tags: readonly string[];
  readonly mediaUrls: readonly string[];
  readonly likesCount: number;
  readonly createdAt: string;
}

export interface AdminComment {
  readonly id: number;
  readonly authorUsername: string;
  readonly authorProfilePictureUrl: string | null;
  readonly content: string;
  readonly isDeleted: boolean;
  readonly createdAt: string;
}
