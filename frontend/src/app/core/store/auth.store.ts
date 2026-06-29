import { Injectable, inject, signal, computed, Signal, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { AuthService, UserDto } from '../services/auth.service';

/**
 * AuthStore — Signal facade over the existing AuthService.
 *
 * AuthService owns the real state (BehaviorSubject + localStorage).
 * This store exposes it as Angular Signals so that OnPush components
 * (NavbarComponent, HomePageComponent) can consume it reactively
 * without subscribing manually.
 *
 * Rule: NEVER duplicate state here. Always derive from AuthService.
 */
@Injectable({ providedIn: 'root' })
export class AuthStore implements OnDestroy {
  readonly #authService = inject(AuthService);

  // Private writable signal — updated whenever AuthService emits
  readonly #user = signal<UserDto | null>(null);
  readonly #sub: Subscription;

  constructor() {
    // Bridge: keep the signal in sync with the BehaviorSubject
    this.#sub = this.#authService.currentUser$.subscribe((user) => {
      this.#user.set(user);
    });
  }

  ngOnDestroy(): void {
    this.#sub.unsubscribe();
  }

  // ── Public read-only signals ───────────────────────────────────────────
  readonly user: Signal<UserDto | null> = computed(() => this.#user());

  readonly isAuthenticated: Signal<boolean> = computed(() => this.#user() !== null);

  /**
   * Username for display — safe to call even when user is null.
   */
  readonly displayName: Signal<string> = computed(() => {
    const u = this.#user();
    return u ? u.username : '';
  });

  /**
   * Avatar URL — returns null when no user or no picture set.
   */
  readonly avatarUrl: Signal<string | null> = computed(
    // Changed from profilePictureUrl to profileImageUrl to match the backend
    () => this.#user()?.profilePictureUrl ?? null,
  );

  // ── Mutations — delegate to AuthService ───────────────────────────────
  logout(): void {
    this.#authService.logout();
    // Signal updates automatically via the BehaviorSubject subscription above
  }
}
