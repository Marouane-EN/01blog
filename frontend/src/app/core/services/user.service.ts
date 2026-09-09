import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CursorResponse, PublicProfile, SubscriptionResponse, UserDto } from '../models';
import { environment } from '../../../environments/environment';
import { buildCursorParams } from '../utils/http.utils';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private http = inject(HttpClient);
  private readonly API_URL = environment.apiUrl;
  getFollowing(userId: number, cursor?: number | null): Observable<CursorResponse<UserDto>> {
    return this.http.get<CursorResponse<UserDto>>(`${this.API_URL}/users/${userId}/following`, {
      params: buildCursorParams(cursor),
    });
  }

  getFollowers(userId: number, cursor?: number | null): Observable<CursorResponse<UserDto>> {
    return this.http.get<CursorResponse<UserDto>>(`${this.API_URL}/users/${userId}/followers`, {
      params: buildCursorParams(cursor),
    });
  }

  getProfile(username: string): Observable<PublicProfile> {
    return this.http.get<PublicProfile>(`${this.API_URL}/users/${username}`);
  }

  toggleSubscription(username: string): Observable<SubscriptionResponse> {
    return this.http.post<SubscriptionResponse>(`${this.API_URL}/users/${username}/subscribe`, {});
  }

  uploadAvatar(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post(`${this.API_URL}/users/me/avatar`, formData, { responseType: 'text' });
  }

  searchUsers(query: string, cursor?: number | null): Observable<CursorResponse<UserDto>> {
    const params = buildCursorParams(cursor, new HttpParams().set('q', query));
    return this.http.get<CursorResponse<UserDto>>(`${this.API_URL}/users/search`, { params });
  }

}
