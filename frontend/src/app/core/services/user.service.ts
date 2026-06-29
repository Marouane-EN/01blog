import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CursorResponse,
  PublicProfile,
  SubscriptionResponse,
  UserPreview,
} from '../models/interfaces/post.model';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/api/users';

  getFollowing(userId: number, cursor?: number | null): Observable<CursorResponse<UserPreview>> {
    return this.http.get<CursorResponse<UserPreview>>(`${this.API_URL}/${userId}/following`, {
      params: this.cursorParams(cursor),
    });
  }

  getFollowers(userId: number, cursor?: number | null): Observable<CursorResponse<UserPreview>> {
    return this.http.get<CursorResponse<UserPreview>>(`${this.API_URL}/${userId}/followers`, {
      params: this.cursorParams(cursor),
    });
  }

  getProfile(username: string): Observable<PublicProfile> {
    return this.http.get<PublicProfile>(`${this.API_URL}/${username}`);
  }

  searchUsers(query: string, cursor?: number | null): Observable<CursorResponse<UserPreview>> {
    let params = new HttpParams().set('q', query);

    if (cursor) {
      params = params.set('cursor', cursor.toString());
    }

    return this.http.get<CursorResponse<UserPreview>>(`${this.API_URL}/search`, { params });
  }

  toggleSubscription(username: string): Observable<SubscriptionResponse> {
    return this.http.post<SubscriptionResponse>(`${this.API_URL}/${username}/subscribe`, {});
  }

  uploadAvatar(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post(`${this.API_URL}/me/avatar`, formData, { responseType: 'text' });
  }

  private cursorParams(cursor?: number | null): HttpParams {
    let params = new HttpParams();

    if (cursor) {
      params = params.set('cursor', cursor.toString());
    }

    return params;
  }
}
