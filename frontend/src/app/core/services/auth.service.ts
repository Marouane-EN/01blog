import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { AuthResponse, LoginRequest, RegistrationRequest, UserDto } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private static readonly TOKEN_KEY = 'jwt_token';
  private static readonly USER_KEY = 'current_user';

  private http = inject(HttpClient);
  private platformId = inject(PLATFORM_ID);
  private readonly API_URL = environment.apiUrl;

  private currentUserSubject = new BehaviorSubject<UserDto | null>(this.getSavedUser());
  currentUser$ = this.currentUserSubject.asObservable();

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.API_URL}/auth/login`, credentials)
      .pipe(tap((response) => this.handleAuthSuccess(response)));
  }

  register(registrationData: RegistrationRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.API_URL}/auth/register`, registrationData)
      .pipe(tap((response) => this.handleAuthSuccess(response)));
  }

  fetchMe(): Observable<UserDto> {
    return this.http.get<UserDto>(`${this.API_URL}/users/me`).pipe(
      tap((userProfile) => {
        this.persistUser(userProfile);
        this.currentUserSubject.next(userProfile);
      }),
    );
  }

  logout(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem(AuthService.TOKEN_KEY);
      localStorage.removeItem(AuthService.USER_KEY);
    }
    this.currentUserSubject.next(null);
  }

  hasToken(): boolean {
    return !!this.getToken();
  }

  getToken(): string | null {
    return isPlatformBrowser(this.platformId) ? localStorage.getItem(AuthService.TOKEN_KEY) : null;
  }

  /**
   * Persists a token obtained outside the normal login/register flow
   * (e.g. an OAuth2 redirect callback that only receives a raw token).
   */
  setToken(token: string): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem(AuthService.TOKEN_KEY, token);
    }
  }

  // --- PRIVATE HELPERS ---

  private handleAuthSuccess(response: AuthResponse): void {
    if (response && response.token) {
      this.setToken(response.token);
      this.persistUser(response.userProfile);
      this.currentUserSubject.next(response.userProfile);
    }
  }

  private persistUser(user: UserDto): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem(AuthService.USER_KEY, JSON.stringify(user));
    }
  }

  private getSavedUser(): UserDto | null {
    if (isPlatformBrowser(this.platformId)) {
      const savedUser = localStorage.getItem(AuthService.USER_KEY);
      try {
        return savedUser ? JSON.parse(savedUser) : null;
      } catch {
        localStorage.removeItem(AuthService.USER_KEY);
        return null;
      }
    }
    return null;
  }
}
