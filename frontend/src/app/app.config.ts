import { ApplicationConfig, provideZonelessChangeDetection } from '@angular/core';
import { provideRouter, withComponentInputBinding, withViewTransitions } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
// Use YOUR existing interceptor — no changes to that file needed
import { jwtInterceptor } from './core/interceptors/jwt.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZonelessChangeDetection(),

    provideRouter(
      routes,
      withComponentInputBinding(), // lets route params map to @Input() automatically
      withViewTransitions(), // smooth page transitions
    ),

    // Register your existing jwt interceptor here
    provideHttpClient(withInterceptors([jwtInterceptor, errorInterceptor])),
  ],
};
