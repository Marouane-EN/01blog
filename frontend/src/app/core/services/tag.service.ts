import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { TagTrend } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class TagService {
  private http = inject(HttpClient);
  private readonly API_URL = environment.apiUrl;

  getTrendingTags(limit = 5): Observable<readonly TagTrend[]> {
    const params = new HttpParams().set('limit', limit);
    return this.http.get<readonly TagTrend[]>(`${this.API_URL}/tags/trending`, { params });
  }
}
