import { Component, inject, PLATFORM_ID, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { ToastComponent } from '../../../shared/components/toast/toast.component';
import { extractErrorMessage } from '../../../core/utils/http-error.utils';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { isPlatformBrowser } from '@angular/common';
import { RegistrationRequest } from '../../../core/models';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatProgressSpinnerModule,
    ToastComponent,
  ],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss'],
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private toastService = inject(ToastService);
  isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  registerForm = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(20)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    bio: ['', [Validators.maxLength(500)]],
    birthDate: [null as Date | null, [Validators.required]],
  });

  isLoading = signal(false);
  errorMessage = signal('');

  onRegister() {
    if (this.registerForm.invalid) return;

    this.isLoading.set(true);

    // Create the clean object
    const registrationData = this.createRegistrationData();

    // Send it to the service
    this.authService.register(registrationData).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.isLoading.set(false);
        const message = extractErrorMessage(err, 'Please check your details.');
        this.errorMessage.set(message);
        this.toastService.error(message);
      },
    });
  }

  // Reuse the OAuth2 triggers from the login component
  loginWithGoogle() {
    window.location.href = 'http://localhost:8080/oauth2/authorization/google';
  }
  loginWithGithub() {
    window.location.href = 'http://localhost:8080/oauth2/authorization/github';
  }
  // Call this method inside your onRegister() function
  private createRegistrationData() {
    // 1. Get raw values from the Reactive Form
    const rawValue = this.registerForm.getRawValue();

    // 2. Format birthDate: Convert JS Date to ISO string, then extract YYYY-MM-DD
    const formattedBirthDate = new Date(rawValue.birthDate as Date).toISOString().split('T')[0];

    // 3. Assemble the payload to match the Spring Boot 'RegisterRequest' record
    const RegistrationRequest: RegistrationRequest = {
      username: rawValue.username,
      email: rawValue.email,
      password: rawValue.password,
      bio: rawValue.bio,
      birthDate: new Date(formattedBirthDate as string), // Ensure it's a Date object
    };

    return RegistrationRequest;
  }
}
