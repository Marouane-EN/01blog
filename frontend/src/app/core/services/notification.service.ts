import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CursorResponse, Notification } from '../models/interfaces/post.model';

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/api/notifications';

  getNotifications(cursor?: number | null): Observable<CursorResponse<Notification>> {
    let params = new HttpParams();

    if (cursor) {
      params = params.set('cursor', cursor.toString());
    }

    return this.http.get<CursorResponse<Notification>>(this.API_URL, { params });
  }

  getUnreadCount(): Observable<number> {
    return this.http.get<number>(`${this.API_URL}/unreadcount`);
  }

  toggleRead(id: number): Observable<Notification> {
    return this.http.put<Notification>(`${this.API_URL}/${id}/readOrUnread`, {});
  }
}
