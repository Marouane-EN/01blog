import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Comment, CreateCommentRequest, CursorResponse, LikeResponse } from '../models';
import { environment } from '../../../environments/environment';
import { buildCursorParams } from '../utils/http.utils';

@Injectable({
  providedIn: 'root',
})
export class CommentService {
  private http = inject(HttpClient);
  private readonly API_URL = environment.apiUrl; // Use the API URL from the environment configuration

  getComments(postId: number, cursor?: number | null): Observable<CursorResponse<Comment>> {
    let params = buildCursorParams(cursor);
    return this.http.get<CursorResponse<Comment>>(`${this.API_URL}/posts/${postId}/comments`, {
      params,
    });
  }

  createComment(postId: number, request: CreateCommentRequest): Observable<Comment> {
    return this.http.post<Comment>(`${this.API_URL}/posts/${postId}/comments`, request);
  }

  updateComment(postId: number, commentId: number, content: CreateCommentRequest): Observable<Comment> {
    return this.http.put<Comment>(`${this.API_URL}/posts/${postId}/comments/${commentId}`, content);
  }

  deleteComment(postId: number, commentId: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(
      `${this.API_URL}/posts/${postId}/comments/${commentId}`,
    );
  }

  toggleLike(postId: number, commentId: number): Observable<LikeResponse> {
    return this.http.post<LikeResponse>(
      `${this.API_URL}/posts/${postId}/comments/${commentId}/likes`,
      {},
    );
  }
}
