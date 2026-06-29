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

export interface AuthUser extends User {
  readonly token: string;
}
