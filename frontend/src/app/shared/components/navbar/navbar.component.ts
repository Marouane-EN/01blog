import {
  Component,
  Input,
  Output,
  EventEmitter,
  ChangeDetectionStrategy,
  signal,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { UserDto } from '../../../core/services/auth.service';

/**
 * NavbarComponent — dumb presentation component.
 *
 * Receives the current user (UserDto from your AuthService) as an @Input.
 * Emits events upward; never touches stores or services directly.
 *
 * Placed in shared/ because it is used by every page layout.
 */
@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header class="navbar" role="banner">
      <nav class="navbar__inner" aria-label="Main navigation">
        <!-- Logo -->
        <a routerLink="/" class="navbar__logo" aria-label="Blog — home">
          <span class="navbar__logo-text">Blog</span>
        </a>

        <!-- Search -->
        <div class="navbar__search-wrap" role="search">
          <label for="global-search" class="sr-only">Search posts</label>
          <svg
            class="navbar__search-icon"
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
            aria-hidden="true"
          >
            <circle cx="11" cy="11" r="8" />
            <path d="m21 21-4.35-4.35" />
          </svg>
          <input
            id="global-search"
            type="search"
            class="navbar__search"
            placeholder="Search posts, tags, people…"
            [(ngModel)]="searchQuery"
            (keyup.enter)="onSearch()"
            autocomplete="off"
          />
        </div>

        <!-- Right side -->
        <div class="navbar__actions">
          @if (user) {
            <!-- Create post -->
            <a
              routerLink="/create-post"
              class="navbar__btn navbar__btn--create"
              aria-label="Create a new post"
            >
              <svg
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2.5"
                stroke-linecap="round"
                stroke-linejoin="round"
                aria-hidden="true"
              >
                <path d="M12 5v14M5 12h14" />
              </svg>
              <span class="navbar__btn-label">Create post</span>
            </a>

            <!-- Notifications -->
            <button
              type="button"
              class="navbar__icon-btn"
              (click)="notificationsClick.emit()"
              aria-label="Notifications"
            >
              <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
                aria-hidden="true"
              >
                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
                <path d="M13.73 21a2 2 0 0 1-3.46 0" />
              </svg>
              @if (notificationsCount > 0) {
                <span class="navbar__badge" [attr.aria-label]="notificationsCount + ' unread'">
                  {{ notificationsCount > 9 ? '9+' : notificationsCount }}
                </span>
              }
            </button>

            <!-- Avatar / profile menu -->
            <div class="navbar__profile-wrap">
              <button
                type="button"
                class="navbar__avatar-btn"
                (click)="profileMenuOpen.update((v) => !v)"
                [attr.aria-expanded]="profileMenuOpen()"
                aria-haspopup="true"
                [attr.aria-label]="user.username + ' profile menu'"
              >
                @if (user.profilePictureUrl) {
                  <img
                    [src]="user.profilePictureUrl"
                    [alt]="user.username"
                    class="navbar__avatar-img"
                    width="32"
                    height="32"
                  />
                } @else {
                  <span class="navbar__avatar-initials" aria-hidden="true">
                    {{ user.username.charAt(0).toUpperCase() }}
                  </span>
                }
              </button>

              @if (profileMenuOpen()) {
                <div
                  class="navbar__profile-menu"
                  role="menu"
                  [attr.aria-label]="user.username + ' menu'"
                  (keydown.escape)="profileMenuOpen.set(false)"
                >
                  <div class="navbar__menu-header">
                    <p class="navbar__menu-name">{{ user.username }}</p>
                  </div>

                  <div class="navbar__menu-divider" role="separator"></div>

                  <a
                    [routerLink]="['/profile', user.username]"
                    class="navbar__menu-item"
                    role="menuitem"
                    (click)="profileMenuOpen.set(false)"
                  >
                    Profile
                  </a>

                  <a
                    routerLink="/settings"
                    class="navbar__menu-item"
                    role="menuitem"
                    (click)="profileMenuOpen.set(false)"
                  >
                    Settings
                  </a>

                  <div class="navbar__menu-divider" role="separator"></div>

                  <button
                    type="button"
                    class="navbar__menu-item navbar__menu-item--danger"
                    role="menuitem"
                    (click)="onLogout()"
                  >
                    Sign out
                  </button>
                </div>
              }
            </div>
          } @else {
            <a routerLink="/login" class="navbar__btn navbar__btn--ghost">Log in</a>
            <a routerLink="/register" class="navbar__btn navbar__btn--primary">Create account</a>
          }
        </div>
      </nav>
    </header>
  `,
  styles: [
    `
      .sr-only {
        position: absolute;
        width: 1px;
        height: 1px;
        padding: 0;
        margin: -1px;
        overflow: hidden;
        clip: rect(0, 0, 0, 0);
        white-space: nowrap;
        border-width: 0;
      }

      .navbar {
        position: sticky;
        top: 0;
        z-index: 100;
        background: #ffffff;
        border-bottom: 1px solid #e5e7eb;
      }

      .navbar__inner {
        display: flex;
        align-items: center;
        gap: 16px;
        max-width: 1280px;
        margin: 0 auto;
        padding: 0 24px;
        height: 56px;
      }

      .navbar__logo {
        text-decoration: none;
        flex-shrink: 0;
      }
      .navbar__logo-text {
        font-size: 20px;
        font-weight: 800;
        color: #4f46e5;
        letter-spacing: -0.5px;
      }

      .navbar__search-wrap {
        position: relative;
        flex: 1;
        max-width: 480px;
      }
      .navbar__search-icon {
        position: absolute;
        left: 12px;
        top: 50%;
        transform: translateY(-50%);
        color: #9ca3af;
        pointer-events: none;
      }
      .navbar__search {
        width: 100%;
        height: 36px;
        padding: 0 12px 0 36px;
        border: 1px solid #e5e7eb;
        border-radius: 6px;
        font-size: 14px;
        color: #111827;
        background: #f9fafb;
        outline: none;
        box-sizing: border-box;
        transition:
          border-color 0.15s,
          background 0.15s;
      }
      .navbar__search:focus {
        border-color: #6366f1;
        background: #ffffff;
        box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.12);
      }

      .navbar__actions {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-shrink: 0;
        margin-left: auto;
      }

      .navbar__btn {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 0 14px;
        height: 36px;
        border-radius: 6px;
        font-size: 14px;
        font-weight: 500;
        text-decoration: none;
        border: none;
        cursor: pointer;
        transition:
          background 0.12s,
          color 0.12s;
        white-space: nowrap;
      }
      .navbar__btn--create {
        color: #4f46e5;
        border: 1.5px solid #c7d2fe;
        background: transparent;
      }
      .navbar__btn--create:hover {
        background: #eef2ff;
        border-color: #a5b4fc;
      }
      .navbar__btn--ghost {
        color: #374151;
        background: transparent;
      }
      .navbar__btn--ghost:hover {
        background: #f3f4f6;
      }
      .navbar__btn--primary {
        color: #ffffff;
        background: #4f46e5;
      }
      .navbar__btn--primary:hover {
        background: #4338ca;
      }

      @media (max-width: 640px) {
        .navbar__btn-label {
          display: none;
        }
        .navbar__btn--create {
          padding: 0 10px;
        }
      }

      .navbar__icon-btn {
        position: relative;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 36px;
        height: 36px;
        border-radius: 6px;
        border: none;
        background: transparent;
        color: #6b7280;
        cursor: pointer;
        transition:
          background 0.12s,
          color 0.12s;
      }
      .navbar__icon-btn:hover {
        background: #f3f4f6;
        color: #111827;
      }

      .navbar__badge {
        position: absolute;
        top: 4px;
        right: 4px;
        min-width: 16px;
        height: 16px;
        padding: 0 4px;
        border-radius: 8px;
        background: #e11d48;
        color: #ffffff;
        font-size: 10px;
        font-weight: 700;
        line-height: 16px;
        text-align: center;
      }

      .navbar__profile-wrap {
        position: relative;
      }

      .navbar__avatar-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 36px;
        height: 36px;
        border-radius: 50%;
        border: none;
        background: #e0e7ff;
        cursor: pointer;
        overflow: hidden;
        padding: 0;
        transition: box-shadow 0.15s;
      }
      .navbar__avatar-btn:hover {
        box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.2);
      }

      .navbar__avatar-img {
        width: 36px;
        height: 36px;
        object-fit: cover;
      }

      .navbar__avatar-initials {
        font-size: 14px;
        font-weight: 700;
        color: #4f46e5;
        text-transform: uppercase;
      }

      .navbar__profile-menu {
        position: absolute;
        top: calc(100% + 8px);
        right: 0;
        min-width: 200px;
        background: #ffffff;
        border: 1px solid #e5e7eb;
        border-radius: 8px;
        box-shadow:
          0 8px 24px rgba(0, 0, 0, 0.1),
          0 2px 6px rgba(0, 0, 0, 0.06);
        overflow: hidden;
        animation: menu-in 0.12s ease;
      }
      @keyframes menu-in {
        from {
          opacity: 0;
          transform: translateY(-4px);
        }
        to {
          opacity: 1;
          transform: translateY(0);
        }
      }

      .navbar__menu-header {
        padding: 12px 16px;
      }
      .navbar__menu-name {
        margin: 0;
        font-size: 14px;
        font-weight: 600;
        color: #111827;
      }

      .navbar__menu-divider {
        height: 1px;
        background: #f3f4f6;
        margin: 4px 0;
      }

      .navbar__menu-item {
        display: block;
        width: 100%;
        padding: 8px 16px;
        font-size: 14px;
        color: #374151;
        text-decoration: none;
        background: transparent;
        border: none;
        text-align: left;
        cursor: pointer;
        transition: background 0.1s;
      }
      .navbar__menu-item:hover {
        background: #f9fafb;
      }
      .navbar__menu-item--danger {
        color: #dc2626;
      }
      .navbar__menu-item--danger:hover {
        background: #fef2f2;
      }
    `,
  ],
})
export class NavbarComponent {
  /** Current user from AuthService — null when logged out */
  @Input() user: UserDto | null = null;
  @Input() notificationsCount = 0;

  @Output() search = new EventEmitter<string>();
  @Output() logout = new EventEmitter<void>();
  @Output() notificationsClick = new EventEmitter<void>();

  protected readonly profileMenuOpen = signal(false);
  protected searchQuery = '';

  protected onSearch(): void {
    const q = this.searchQuery.trim();
    if (q) this.search.emit(q);
  }

  protected onLogout(): void {
    this.profileMenuOpen.set(false);
    this.logout.emit();
  }
}
