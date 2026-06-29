import { Component, OnInit, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <a href="#main-content" class="skip-link">Skip to main content</a>
    <router-outlet />
  `,
  styles: [
    `
      .skip-link {
        position: absolute;
        top: -40px;
        left: 8px;
        z-index: 9999;
        padding: 8px 16px;
        background: #4f46e5;
        color: #ffffff;
        border-radius: 0 0 6px 6px;
        font-size: 14px;
        text-decoration: none;
        transition: top 0.1s ease;
      }
      .skip-link:focus {
        top: 0;
      }
    `,
  ],
})
export class AppComponent implements OnInit {
  // Use YOUR existing AuthService — not AuthStore — for the boot call
  readonly #authService = inject(AuthService);

  ngOnInit(): void {
    // If a JWT token exists in localStorage, verify it with the backend
    // and rehydrate the current user (navbar will update automatically
    // because AuthStore subscribes to AuthService.currentUser$).
    const token = typeof window !== 'undefined' ? localStorage.getItem('jwt_token') : null;

    if (token) {
      this.#authService.fetchMe().subscribe({
        // Success: AuthService updates currentUserSubject → AuthStore signal updates → Navbar re-renders
        error: () => {
          // Token is expired or invalid — clean up
          this.#authService.logout();
        },
      });
    }
  }
}
