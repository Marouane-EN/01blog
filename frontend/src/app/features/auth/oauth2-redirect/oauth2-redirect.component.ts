import { Component, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
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
  private platformId = inject(PLATFORM_ID);

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      const token = this.route.snapshot.queryParamMap.get('token');

      if (token) {
        // 1. Clean the token just in case
        const cleanToken = token.replace(/['"]+/g, '');

        // 2. Save it using the exact key your AuthService expects
        localStorage.setItem('jwt_token', cleanToken);

        // 3. DO NOT use this.router.navigate(['/']) or fetchMe() here!
        // Force the browser to do a hard refresh. When it wakes back up,
        // the AuthStore will find the token and seamlessly log you in.
        window.location.href = '/';
      } else {
        this.router.navigate(['/login']);
      }
    }
  }
}
