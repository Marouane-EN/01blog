import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ReportService {
  private http = inject(HttpClient);
  private readonly API_URL = environment.apiUrl;

  reportPost(postId: number, reason: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.API_URL}/posts/${postId}/reports`, {
      reason,
    });
  }

  reportComment(commentId: number, reason: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.API_URL}/comments/${commentId}/reports`, {
      reason,
    });
  }

  reportUser(userId: number, reason: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.API_URL}/users/${userId}/reports`, {
      reason,
    });
  }
}
