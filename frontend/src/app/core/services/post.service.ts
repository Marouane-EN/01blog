import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  CreatePostRequest,
  CursorResponse,
  FeedTab,
  LikeResponse,
  Post,
  PostMedia,
  PostSearchResult,
} from '../models';
import { environment } from '../../../environments/environment';
import { buildCursorParams } from '../utils/http.utils';

@Injectable({
  providedIn: 'root',
})
export class PostService {
  private http = inject(HttpClient);
  private readonly API_URL = environment.apiUrl; // Use the API URL from the environment configuration

  getFeed(tab: FeedTab, cursor?: number | null): Observable<CursorResponse<Post>> {
    const params = buildCursorParams(cursor);
    const endpoint = tab === 'following' ? '/posts/subscriptions' : '/posts';
    return this.http.get<CursorResponse<Post>>(`${this.API_URL}${endpoint}`, { params });
  }

  getPostsByUser(userId: number, cursor?: number | null): Observable<CursorResponse<Post>> {
    return this.http.get<CursorResponse<Post>>(`${this.API_URL}/posts/user/${userId}`, {
      params: buildCursorParams(cursor),
    });
  }

  getPostBySlug(slug: string): Observable<Post> {
    return this.http.get<Post>(`${this.API_URL}/posts/${slug}`);
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
    const params = buildCursorParams(cursor, new HttpParams().set('q', query));
    return this.http.get<CursorResponse<PostSearchResult>>(`${this.API_URL}/posts/search`, {
      params,
    });
  }

  addFileToPost(postId: number, file: File): Observable<PostMedia> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http
      .post<{ mediaId: number; url: string }>(`${this.API_URL}/posts/${postId}/files`, formData)
      .pipe(map((response) => ({ id: response.mediaId, url: response.url })));
  }

  deleteFileFromPost(postId: number, mediaId: number): Observable<string> {
    return this.http.delete(`${this.API_URL}/posts/${postId}/files/${mediaId}`, {
      responseType: 'text',
    });
  }
}
