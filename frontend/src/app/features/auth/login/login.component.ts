import { Component, inject, PLATFORM_ID, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { extractErrorMessage } from '../../../core/utils/http-error.utils';

import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  private toastService = inject(ToastService);

  loginForm = this.fb.nonNullable.group({
    identifier: ['', [Validators.required]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  hidePassword = signal(true);
  isLoading = signal(false);
  errorMessage = signal('');

  private readonly BACKEND_URL = 'http://localhost:8080';

  loginWithGoogle() {
    window.location.href = `${this.BACKEND_URL}/oauth2/authorization/google`;
  }

  loginWithGithub() {
    window.location.href = `${this.BACKEND_URL}/oauth2/authorization/github`;
  }

  onSubmit() {
    if (this.loginForm.invalid) return;

    this.isLoading.set(true);
    this.errorMessage.set('');

    this.authService.login(this.loginForm.getRawValue()).subscribe({
      next: () => {
        this.isLoading.set(false);
        // CHANGE 2: navigate to home after successful login
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.isLoading.set(false);
        const message = extractErrorMessage(
          err,
          'An unexpected error occurred. Please try again later.',
        );
        this.errorMessage.set(message);
        this.toastService.error(message);
      },
    });
  }
}
