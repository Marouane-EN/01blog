import { DatePipe, isPlatformBrowser } from '@angular/common';
import { Component, inject, OnInit, PLATFORM_ID, signal } from '@angular/core';
import { AdminPost } from '../../../core/models';
import { AdminService } from '../../../core/services/admin.service';
import { AvatarComponent } from '../../../shared/components/avatar/avatar.component';
import { ConfirmDialogService } from '../../../core/services/confirm-dialog.service';
import { ToastService } from '../../../core/services/toast.service';
import { extractErrorMessage } from '../../../core/utils/http-error.utils';

const PAGE_SIZE = 20;

@Component({
  selector: 'app-manage-posts',
  standalone: true,
  imports: [DatePipe, AvatarComponent],
  templateUrl: './manage-posts.component.html',
  styleUrl: './manage-posts.component.scss',
})
export class ManagePostsComponent implements OnInit {
  private adminService = inject(AdminService);
  private confirmDialog = inject(ConfirmDialogService);
  private toastService = inject(ToastService);
  private platformId = inject(PLATFORM_ID);
  private searchDebounce?: ReturnType<typeof setTimeout>;

  posts = signal<readonly AdminPost[]>([]);
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

  toggleVisibility(post: AdminPost): void {
    const isHide = !post.isHidden;
    this.confirmDialog
      .confirm({
        title: `${isHide ? 'Hide' : 'Restore'} this post?`,
        description: isHide
          ? `"${post.title}" will be hidden from the public feed. You can restore it later.`
          : `"${post.title}" will become visible on 01 again.`,
        confirmLabel: isHide ? 'Hide post' : 'Restore post',
        danger: isHide,
      })
      .then((confirmed) => {
        if (!confirmed) {
          return;
        }
        this.adminService.togglePostVisibility(post.id).subscribe({
          next: (res) => {
            this.updatePost(post.id, { isHidden: isHide });
            this.toastService.success(res.message);
          },
          error: (err) =>
            this.toastService.error(extractErrorMessage(err, 'Something went wrong. Please try again.')),
        });
      });
  }

  deletePost(post: AdminPost): void {
    this.confirmDialog
      .confirm({
        title: 'Delete this post?',
        description: `"${post.title}" will be permanently removed from 01. This can't be undone.`,
        confirmLabel: 'Delete post',
      })
      .then((confirmed) => {
        if (!confirmed) {
          return;
        }
        this.adminService.hardDeletePost(post.id).subscribe({
          next: (res) => {
            this.posts.update((items) => items.filter((p) => p.id !== post.id));
            this.toastService.success(res.message);
          },
          error: (err) =>
            this.toastService.error(extractErrorMessage(err, 'Something went wrong. Please try again.')),
        });
      });
  }

  private updatePost(postId: number, changes: Partial<AdminPost>): void {
    this.posts.update((items) => items.map((p) => (p.id === postId ? { ...p, ...changes } : p)));
  }

  private loadPage(page: number): void {
    const isFirstPage = page === 0;
    isFirstPage ? this.loading.set(true) : this.loadingMore.set(true);

    this.adminService.getPosts(page, PAGE_SIZE, this.search() || undefined).subscribe({
      next: (response) => {
        this.posts.update((items) =>
          isFirstPage ? response.content : [...items, ...response.content],
        );
        this.page.set(response.number);
        this.isLast.set(response.last);
        this.loading.set(false);
        this.loadingMore.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.loadingMore.set(false);
        this.toastService.error(extractErrorMessage(err, 'Could not load posts. Please try again.'));
      },
    });
  }
}
