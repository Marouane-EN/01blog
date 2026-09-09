import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AdminAction,
  AdminComment,
  AdminPost,
  AdminPostDetails,
  AdminReport,
  AdminUser,
  PageResponse,
} from '../models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AdminService {
  private http = inject(HttpClient);
  private readonly API_URL = environment.apiUrl;

  getPendingReports(page = 0, size = 20): Observable<PageResponse<AdminReport>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<AdminReport>>(`${this.API_URL}/admin/reports`, { params });
  }

  resolveReport(reportId: number, action: AdminAction): Observable<string> {
    return this.http.put(
      `${this.API_URL}/admin/reports/${reportId}/resolve`,
      { action },
      { responseType: 'text' },
    );
  }

  getUsers(page = 0, size = 20, search?: string): Observable<PageResponse<AdminUser>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (search) {
      params = params.set('search', search);
    }
    return this.http.get<PageResponse<AdminUser>>(`${this.API_URL}/admin/users`, { params });
  }

  getUserPosts(userId: number, page = 0, size = 20): Observable<PageResponse<AdminPost>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<AdminPost>>(`${this.API_URL}/admin/users/${userId}`, {
      params,
    });
  }

  getPosts(page = 0, size = 20, search?: string): Observable<PageResponse<AdminPost>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (search) {
      params = params.set('search', search);
    }
    return this.http.get<PageResponse<AdminPost>>(`${this.API_URL}/admin/posts`, { params });
  }

  getPostDetails(postId: number): Observable<AdminPostDetails> {
    return this.http.get<AdminPostDetails>(`${this.API_URL}/admin/posts/${postId}`);
  }

  getPostComments(postId: number, page = 0, size = 20): Observable<PageResponse<AdminComment>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<AdminComment>>(
      `${this.API_URL}/admin/posts/${postId}/comments`,
      { params },
    );
  }

  toggleUserBan(userId: number): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.API_URL}/admin/users/${userId}/ban`, {});
  }

  toggleUserDelete(userId: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.API_URL}/admin/users/${userId}`);
  }

  togglePostVisibility(postId: number): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.API_URL}/admin/posts/${postId}/hide`, {});
  }

  hardDeletePost(postId: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.API_URL}/admin/posts/${postId}`);
  }

  deleteComment(commentId: number): Observable<string> {
    return this.http.delete(`${this.API_URL}/admin/comments/${commentId}`, {
      responseType: 'text',
    });
  }
}
