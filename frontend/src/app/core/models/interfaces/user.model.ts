export interface User {
  readonly id: string;
  readonly username: string;
  readonly email: string;
  readonly displayName: string;
  readonly profilePictureUrl: string | null;
  readonly bio: string | null;
  readonly followersCount: number;
  readonly followingCount: number;
  readonly createdAt: string;
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

export interface SubscriptionResponse {
  readonly isSubscribed: boolean;
  readonly totalSubscribers: number;
}

export interface UserDto {
  id: number;
  username: string;
  profilePictureUrl: string | null;
}
