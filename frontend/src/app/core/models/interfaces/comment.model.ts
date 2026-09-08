import { UserDto } from "./user.model";

export interface Comment {
  readonly id: number;
  readonly content: string;
  readonly author: UserDto;
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
