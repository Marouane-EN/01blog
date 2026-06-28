import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FeedTab, PostFeed } from '../models';

@Injectable({ providedIn: 'root' })
export class PostService {
  readonly #http = inject(HttpClient);
  readonly #baseUrl = '/api/posts';

  getFeed(tab: FeedTab, cursor?: string | null): Observable<PostFeed> {
    let params = new HttpParams().set('tab', tab);
    if (cursor) {
      params = params.set('cursor', cursor);
    }
    return this.#http.get<PostFeed>(this.#baseUrl, { params });
  }

  likePost(postId: string): Observable<{ likesCount: number }> {
    return this.#http.post<{ likesCount: number }>(`${this.#baseUrl}/${postId}/like`, {});
  }

  unlikePost(postId: string): Observable<{ likesCount: number }> {
    return this.#http.delete<{ likesCount: number }>(`${this.#baseUrl}/${postId}/like`);
  }

  bookmarkPost(postId: string): Observable<void> {
    return this.#http.post<void>(`${this.#baseUrl}/${postId}/bookmark`, {});
  }

  removeBookmark(postId: string): Observable<void> {
    return this.#http.delete<void>(`${this.#baseUrl}/${postId}/bookmark`);
  }
}
