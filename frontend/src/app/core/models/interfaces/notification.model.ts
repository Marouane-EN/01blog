export interface Notification {
  readonly id: number;
  readonly senderUsername: string;
  readonly senderProfilePictureUrl: string | null;
  readonly type: string;
  readonly postId: number | null;
  readonly isRead: boolean;
  readonly createdAt: string;
}
