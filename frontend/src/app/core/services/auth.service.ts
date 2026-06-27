import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';

export interface LoginRequest {
  identifier: string;
  password: string;
}

export interface UserDto {
  id: number;
  username: string;
  profilePictureUrl?: string;
}

export interface AuthResponse {
  token: string;
  userProfile: UserDto;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/api/auth';

  // UPGRADE: Store the actual User profile instead of just true/false
  private currentUserSubject = new BehaviorSubject<UserDto | null>(this.getSavedUser());

  // The Navbar will subscribe to this to get the user's username and picture
  currentUser$ = this.currentUserSubject.asObservable();

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, credentials).pipe(
      tap((response) => {
        if (response && response.token) {
          // HOW IT IS STORED: We take the token string and save it under the key 'jwt_token'
          localStorage.setItem('jwt_token', response.token);

          // We convert the user object into a string and save it so it survives page refreshes
          localStorage.setItem('current_user', JSON.stringify(response.userProfile));

          // Broadcast the new user to the rest of the application
          this.currentUserSubject.next(response.userProfile);
        }
      }),
    );
  }

  logout(): void {
    // Clear everything out on logout
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('current_user');
    this.currentUserSubject.next(null);
  }

  // Helper to check LocalStorage when the app first boots up
  private getSavedUser(): UserDto | null {
    if (typeof window !== 'undefined' && window.localStorage) {
      const savedUser = localStorage.getItem('current_user');
      return savedUser ? JSON.parse(savedUser) : null;
    }
    return null;
  }
}
