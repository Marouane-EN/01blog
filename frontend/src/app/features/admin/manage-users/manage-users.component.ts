import { DatePipe, isPlatformBrowser } from '@angular/common';
import { Component, inject, OnInit, PLATFORM_ID, signal } from '@angular/core';
import { AdminUser } from '../../../core/models';
import { AdminService } from '../../../core/services/admin.service';
import { AvatarComponent } from '../../../shared/components/avatar/avatar.component';
import { AdminUiService } from '../shared/admin-ui.service';

const PAGE_SIZE = 20;

@Component({
  selector: 'app-manage-users',
  standalone: true,
  imports: [DatePipe, AvatarComponent],
  templateUrl: './manage-users.component.html',
  styleUrl: './manage-users.component.scss',
})
export class ManageUsersComponent implements OnInit {
  private adminService = inject(AdminService);
  private ui = inject(AdminUiService);
  private platformId = inject(PLATFORM_ID);
  private searchDebounce?: ReturnType<typeof setTimeout>;

  users = signal<readonly AdminUser[]>([]);
  loading = signal(true);
  loadingMore = signal(false);
  page = signal(0);
  isLast = signal(true);
  search = signal('');

  ngOnInit(): void {
    // The JWT lives in localStorage, which doesn't exist during SSR — skip
    // the fetch there instead of firing an unauthenticated request.
    if (isPlatformBrowser(this.platformId)) {
      this.loadPage(0);
    }
  }

  onSearchInput(value: string): void {
    clearTimeout(this.searchDebounce);
    this.searchDebounce = setTimeout(() => {
      this.search.set(value.trim());
      this.loadPage(0);
    }, 350);
  }

  loadMore(): void {
    if (this.isLast() || this.loadingMore()) {
      return;
    }
    this.loadPage(this.page() + 1);
  }

  toggleBan(user: AdminUser): void {
    const isBan = !user.isBlocked;
    this.ui
      .confirm({
        title: `${isBan ? 'Ban' : 'Unban'} ${user.username}?`,
        description: isBan
          ? `${user.username} will no longer be able to sign in or post on 01. You can unban them later.`
          : `${user.username} will regain full access to 01.`,
        confirmLabel: isBan ? 'Ban user' : 'Unban user',
        danger: isBan,
      })
      .then((confirmed) => {
        if (!confirmed) {
          return;
        }
        this.adminService.toggleUserBan(user.id).subscribe({
          next: (res) => {
            this.updateUser(user.id, { isBlocked: isBan });
            this.ui.showToast(res.message);
          },
          error: () => this.ui.showToast('Something went wrong. Please try again.'),
        });
      });
  }

  toggleActive(user: AdminUser): void {
    const isDeactivate = user.isActive;
    this.ui
      .confirm({
        title: `${isDeactivate ? 'Deactivate' : 'Restore'} ${user.username}'s account?`,
        description: isDeactivate
          ? `${user.username}'s account will be deactivated.`
          : `${user.username}'s account will be restored.`,
        confirmLabel: isDeactivate ? 'Deactivate' : 'Restore',
        danger: isDeactivate,
      })
      .then((confirmed) => {
        if (!confirmed) {
          return;
        }
        this.adminService.toggleUserDelete(user.id).subscribe({
          next: (res) => {
            this.updateUser(user.id, { isActive: !isDeactivate });
            this.ui.showToast(res.message);
          },
          error: () => this.ui.showToast('Something went wrong. Please try again.'),
        });
      });
  }

  private updateUser(userId: number, changes: Partial<AdminUser>): void {
    this.users.update((items) =>
      items.map((u) => (u.id === userId ? { ...u, ...changes } : u)),
    );
  }

  private loadPage(page: number): void {
    const isFirstPage = page === 0;
    isFirstPage ? this.loading.set(true) : this.loadingMore.set(true);

    this.adminService.getUsers(page, PAGE_SIZE, this.search() || undefined).subscribe({
      next: (response) => {
        this.users.update((items) =>
          isFirstPage ? response.content : [...items, ...response.content],
        );
        this.page.set(response.number);
        this.isLast.set(response.last);
        this.loading.set(false);
        this.loadingMore.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.loadingMore.set(false);
      },
    });
  }
}
