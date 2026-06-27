import { Component, inject, ChangeDetectorRef } from '@angular/core'; // <-- Add ChangeDetectorRef
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
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

  // THE FIX: Inject the Change Detector
  private cdr = inject(ChangeDetectorRef);

  loginForm = this.fb.nonNullable.group({
    identifier: ['', [Validators.required]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  hidePassword = true;
  isLoading = false;
  errorMessage = '';

  onSubmit() {
    if (this.loginForm.invalid) return;

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login(this.loginForm.getRawValue()).subscribe({
      next: () => {
        // 1. Stop the spinner
        this.isLoading = false;

        // 2. Show a success message instead of trying to route
        this.errorMessage = '✅ Login successful! JWT Token saved.';

        // 3. Force the UI to update instantly
        this.cdr.detectChanges();
      },
      error: (err) => {
        // 3. Stop the spinner on error
        this.isLoading = false;

        if (err.status === 401 || err.status === 403) {
          this.errorMessage = 'Invalid username or password.';
        } else {
          this.errorMessage = 'An unexpected error occurred. Please try again later.';
        }

        // THE FIX: Manually wake up Angular to show the error instantly
        this.cdr.detectChanges();
      },
    });
  }
}
