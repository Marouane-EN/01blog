import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Post,
  CursorResponse,
  FeedTab,
  CreatePostRequest,
  LikeResponse,
  PostSearchResult,
} from '../models/interfaces/post.model';

@Injectable({
  providedIn: 'root',
})
export class PostService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/api/posts';

  getFeed(tab: FeedTab, cursor?: number | null): Observable<CursorResponse<Post>> {
    let params = new HttpParams();
    if (cursor) {
      params = params.set('cursor', cursor.toString());
    }

    // Route to the correct endpoint based on the tab
    if (tab === 'following') {
      return this.http.get<CursorResponse<Post>>(`${this.API_URL}/subscriptions`, { params });
    }

    // Default to latest
    return this.http.get<CursorResponse<Post>>(this.API_URL, { params });
  }

  getPostBySlug(slug: string): Observable<Post> {
    return this.http.get<Post>(`${this.API_URL}/${slug}`);
  }

  getPostsByUser(userId: number, cursor?: number | null): Observable<CursorResponse<Post>> {
    let params = new HttpParams();

    if (cursor) {
      params = params.set('cursor', cursor.toString());
    }

    return this.http.get<CursorResponse<Post>>(`${this.API_URL}/user/${userId}`, { params });
  }

  createPost(request: CreatePostRequest, files: readonly File[] = []): Observable<Post> {
    const formData = new FormData();
    const payload = {
      title: request.title,
      content: request.content,
      tags: request.tags,
    };

    formData.append(
      'postData',
      new Blob([JSON.stringify(payload)], {
        type: 'application/json',
      }),
    );

    files.forEach((file) => formData.append('files', file));

    return this.http.post<Post>(this.API_URL, formData);
  }

  updatePost(postId: number, request: CreatePostRequest): Observable<Post> {
    return this.http.put<Post>(`${this.API_URL}/${postId}`, {
      title: request.title,
      content: request.content,
      tags: request.tags,
    });
  }

  deletePost(postId: number): Observable<string> {
    return this.http.delete(`${this.API_URL}/${postId}`, { responseType: 'text' });
  }

  toggleLike(postId: number): Observable<LikeResponse> {
    return this.http.post<LikeResponse>(`${this.API_URL}/${postId}/like`, {});
  }

  searchPosts(query: string, cursor?: number | null): Observable<CursorResponse<PostSearchResult>> {
    let params = new HttpParams().set('q', query);

    if (cursor) {
      params = params.set('cursor', cursor.toString());
    }

    return this.http.get<CursorResponse<PostSearchResult>>(`${this.API_URL}/search`, { params });
  }
}
