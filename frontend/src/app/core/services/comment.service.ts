import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Comment,
  CreateCommentRequest,
  CursorResponse,
  LikeResponse,
} from '../models/interfaces/post.model';

@Injectable({
  providedIn: 'root',
})
export class CommentService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/api/posts';

  getComments(postId: number, cursor?: number | null): Observable<CursorResponse<Comment>> {
    let params = new HttpParams();

    if (cursor) {
      params = params.set('cursor', cursor.toString());
    }

    return this.http.get<CursorResponse<Comment>>(`${this.API_URL}/${postId}/comments`, { params });
  }

  createComment(postId: number, request: CreateCommentRequest): Observable<Comment> {
    return this.http.post<Comment>(`${this.API_URL}/${postId}/comments`, request);
  }

  updateComment(postId: number, commentId: number, content: string): Observable<Comment> {
    return this.http.put<Comment>(`${this.API_URL}/${postId}/comments/${commentId}`, content);
  }

  deleteComment(postId: number, commentId: number): Observable<string> {
    return this.http.delete(`${this.API_URL}/${postId}/comments/${commentId}`, {
      responseType: 'text',
    });
  }

  toggleLike(postId: number, commentId: number): Observable<LikeResponse> {
    return this.http.post<LikeResponse>(`${this.API_URL}/${postId}/comments/${commentId}/likes`, {});
  }
}
