import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Post, CursorResponse, FeedTab } from '../models/interfaces/post.model';

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
}
