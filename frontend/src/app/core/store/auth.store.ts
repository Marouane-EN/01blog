import { Injectable, inject, signal, computed, Signal, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { UserDto } from '../models';
import { decodeJwtPayload } from '../utils/jwt.utils';

interface JwtPayload {
  role?: string;
}

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
  readonly #router = inject(Router);

  // Private writable signal — updated whenever AuthService emits
  readonly #user = signal<UserDto | null>(null);
  readonly #hasJwt = signal<boolean>(false);
  readonly #sub: Subscription;

  constructor() {
    this.#hasJwt.set(this.#authService.hasToken());

    // Bridge: keep the signal in sync with the BehaviorSubject
    this.#sub = this.#authService.currentUser$.subscribe((user) => {
      this.#user.set(user);
      this.#hasJwt.set(this.#authService.hasToken());
    });

    if (this.#hasJwt() && !this.#user()) {
      this.#authService.fetchMe().subscribe({
        error: () => this.logout(),
      });
    }
  }

  ngOnDestroy(): void {
    this.#sub.unsubscribe();
  }

  // ── Public read-only signals ───────────────────────────────────────────
  readonly user: Signal<UserDto | null> = computed(() => this.#user());

  readonly isAuthenticated: Signal<boolean> = computed(() => this.#user() !== null || this.#hasJwt());

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
    () => this.#user()?.profilePictureUrl ?? null,
  );

  /**
   * True when the JWT carries a ROLE_ADMIN claim.
   * Reads #hasJwt() purely to establish a reactive dependency — the actual
   * role lives inside the token, not on the UserDto.
   */
  readonly isAdmin: Signal<boolean> = computed(() => {
    if (!this.#hasJwt()) {
      return false;
    }
    const token = this.#authService.getToken();
    const payload = token ? decodeJwtPayload<JwtPayload>(token) : null;
    return payload?.role?.split(',').includes('ROLE_ADMIN') ?? false;
  });

  // ── Mutations — delegate to AuthService ───────────────────────────────
  logout(): void {
    this.#authService.logout();
    this.#hasJwt.set(false);
    this.#router.navigate(['/login']);
    // Signal updates automatically via the BehaviorSubject subscription above
  }
}
