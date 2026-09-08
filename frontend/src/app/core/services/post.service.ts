import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CreatePostRequest,
  CursorResponse,
  FeedTab,
  LikeResponse,
  Post,
  PostSearchResult,
} from '../models';
import { environment } from '../../../environments/environment.development';

@Injectable({
  providedIn: 'root',
})
export class PostService {
  private http = inject(HttpClient);
  private readonly API_URL = environment.apiUrl; // Use the API URL from the environment configuration

  getFeed(tab: FeedTab, cursor?: number | null): Observable<CursorResponse<Post>> {
    let params = new HttpParams();
    if (cursor) {
      params = params.set('cursor', cursor.toString());
    }

    // Route to the correct endpoint based on the tab
    if (tab === 'following') {
      return this.http.get<CursorResponse<Post>>(`${this.API_URL}/posts/subscriptions`, { params });
    }

    // Default to latest
    return this.http.get<CursorResponse<Post>>(`${this.API_URL}/posts`, { params });
  }

  getPostBySlug(slug: string): Observable<Post> {
    return this.http.get<Post>(`${this.API_URL}/posts/${slug}`);
  }

  getPostsByUser(userId: number, cursor?: number | null): Observable<CursorResponse<Post>> {
    let params = new HttpParams();

    if (cursor) {
      params = params.set('cursor', cursor.toString());
    }

    return this.http.get<CursorResponse<Post>>(`${this.API_URL}/posts/user/${userId}`, { params });
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

    return this.http.post<Post>(`${this.API_URL}/posts`, formData);
  }

  updatePost(postId: number, request: CreatePostRequest): Observable<Post> {
    return this.http.put<Post>(`${this.API_URL}/posts/${postId}`, {
      title: request.title,
      content: request.content,
      tags: request.tags,
    });
  }

  deletePost(postId: number): Observable<string> {
    return this.http.delete(`${this.API_URL}/posts/${postId}`, { responseType: 'text' });
  }

  toggleLike(postId: number): Observable<LikeResponse> {
    return this.http.post<LikeResponse>(`${this.API_URL}/posts/${postId}/like`, {});
  }

  searchPosts(query: string, cursor?: number | null): Observable<CursorResponse<PostSearchResult>> {
    let params = new HttpParams().set('q', query);

    if (cursor) {
      params = params.set('cursor', cursor.toString());
    }

    return this.http.get<CursorResponse<PostSearchResult>>(`${this.API_URL}/posts/search`, {
      params,
    });
  }
}
