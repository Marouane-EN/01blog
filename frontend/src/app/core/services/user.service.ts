import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CursorResponse, PublicProfile, SubscriptionResponse, UserDto } from '../models';
import { environment } from '../../../environments/environment.development';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private http = inject(HttpClient);
  private readonly API_URL = environment.apiUrl;
  getFollowing(userId: number, cursor?: number | null): Observable<CursorResponse<UserDto>> {
    return this.http.get<CursorResponse<UserDto>>(`${this.API_URL}/users/${userId}/following`, {
      params: this.cursorParams(cursor),
    });
  }

  getFollowers(userId: number, cursor?: number | null): Observable<CursorResponse<UserDto>> {
    return this.http.get<CursorResponse<UserDto>>(`${this.API_URL}/users/${userId}/followers`, {
      params: this.cursorParams(cursor),
    });
  }

  getProfile(username: string): Observable<PublicProfile> {
    return this.http.get<PublicProfile>(`${this.API_URL}/users/${username}`);
  }

  searchUsers(query: string, cursor?: number | null): Observable<CursorResponse<UserDto>> {
    let params = new HttpParams().set('q', query);

    if (cursor) {
      params = params.set('cursor', cursor.toString());
    }

    return this.http.get<CursorResponse<UserDto>>(`${this.API_URL}/users/search`, { params });
  }

  toggleSubscription(username: string): Observable<SubscriptionResponse> {
    return this.http.post<SubscriptionResponse>(`${this.API_URL}/users/${username}/subscribe`, {});
  }

  uploadAvatar(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post(`${this.API_URL}/users/me/avatar`, formData, { responseType: 'text' });
  }

  private cursorParams(cursor?: number | null): HttpParams {
    let params = new HttpParams();

    if (cursor) {
      params = params.set('cursor', cursor.toString());
    }

    return params;
  }
}
