import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-oauth2-redirect',
  standalone: true,
  imports: [CommonModule, MatProgressSpinnerModule],
  template: `
    <div class="redirect-container">
      <mat-spinner diameter="40"></mat-spinner>
      <h2>Authenticating with Social Login...</h2>
      <p>Please wait while we log you in.</p>
    </div>
  `,
  styles: [
    `
      .redirect-container {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 100vh;
        background-color: #f5f5f5;
        font-family:
          -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
      }
      h2 {
        margin-top: 1.5rem;
        color: #333;
      }
      p {
        color: #666;
      }
    `,
  ],
})
export class Oauth2RedirectComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);

  ngOnInit(): void {
    // 1. Grab the token from the URL (e.g., ?token=XYZ)
    const token = this.route.snapshot.queryParamMap.get('token');

    if (token) {
      localStorage.setItem('jwt_token', token);

      this.authService.fetchMe().subscribe({
        next: () => {
          // Success! The user profile is loaded. Send them to the Home Page.
          // this.router.navigate(['/']);
          console.log('OAuth2 login successful! User profile fetched.');
        },
        error: (err) => {
          console.error('Failed to fetch user profile after OAuth2 login', err);
          this.router.navigate(['/login']);
        },
      });
    } else {
      // If someone just types this URL in manually without a token, kick them to login
      this.router.navigate(['/login']);
    }
  }
}
