import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CursorResponse, Notification } from '../models';
import { environment } from '../../../environments/environment';
import { buildCursorParams } from '../utils/http.utils';

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private http = inject(HttpClient);
  private readonly API_URL = environment.apiUrl; // Use the API URL from the environment configuration

  getNotifications(cursor?: number | null): Observable<CursorResponse<Notification>> {
    let params = buildCursorParams(cursor);

    return this.http.get<CursorResponse<Notification>>(`${this.API_URL}/notifications`, { params });
  }

  getUnreadCount(): Observable<number> {
    return this.http.get<number>(`${this.API_URL}/notifications/unreadcount`);
  }

  toggleRead(id: number): Observable<Notification> {
    return this.http.put<Notification>(`${this.API_URL}/notifications/${id}/readOrUnread`, {});
  }
}
