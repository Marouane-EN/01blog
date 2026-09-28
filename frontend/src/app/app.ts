import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class AppComponent implements OnInit {
  // Use YOUR existing AuthService — not AuthStore — for the boot call
  readonly #authService = inject(AuthService);
  readonly isCheckingAuth = signal(this.#authService.hasToken());

  ngOnInit(): void {
    // If a JWT token exists in localStorage, verify it with the backend
    // and rehydrate the current user (navbar will update automatically
    // because AuthStore subscribes to AuthService.currentUser$).
    if (this.#authService.getToken()) {
      this.#authService.fetchMe().subscribe({
        next: () => this.isCheckingAuth.set(false),
        // Success: AuthService updates currentUserSubject → AuthStore signal updates → Navbar re-renders
        error: () => {
          // Token is expired or invalid — clean up
          // this.#authService.logout();
          // this.isCheckingAuth.set(false);
        },
      });
    } else {
      this.isCheckingAuth.set(false);
    }
  }
}
