export interface User {
  readonly id: string;
  readonly username: string;
  readonly email: string;
  readonly displayName: string;
  readonly avatarUrl: string | null;
  readonly bio: string | null;
  readonly followersCount: number;
  readonly followingCount: number;
  readonly createdAt: string; // ISO 8601
}

export interface AuthUser extends User {
  readonly token: string;
}

export type UserPreview = Pick<User, 'id' | 'username' | 'displayName' | 'avatarUrl'>;
