import { Component, OnInit, inject, ChangeDetectionStrategy } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthStore } from '../../core/store/auth.store';
import { FeedStore } from '../../core/store/feed.store';
import { PostService } from '../../core/services/post.service';
import { FeedTab } from '../../core/models/interfaces/post.model';
import { NavbarComponent } from '../../shared/components/navbar/navbar.component';
import { PostFeedComponent } from './post-feed.component';
import { FeedTabsComponent } from './feed-tabs.component';

/**
 * HomePageComponent — smart container.
 *
 * Uses AuthStore (Signal facade) for reactive user state,
 * and your existing AuthService underneath for actual auth operations.
 */
@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [RouterLink, NavbarComponent, PostFeedComponent, FeedTabsComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-navbar
      [user]="authStore.user()"
      [notificationsCount]="0"
      (search)="onSearch($event)"
      (logout)="onLogout()"
      (notificationsClick)="onNotificationsClick()"
    />

    <main class="home-layout" id="main-content">
      <!-- Sidebar: trending tags -->
      <aside class="home-layout__sidebar" aria-label="Trending tags">
        <div class="sidebar-card">
          <h2 class="sidebar-card__title">Trending tags</h2>
          <ul class="sidebar-card__list" role="list">
            @for (tag of trendingTags; track tag.name) {
              <li class="sidebar-card__tag-item" role="listitem">
                <a
                  [routerLink]="['/t', tag.name]"
                  class="sidebar-card__tag-link"
                  [style.color]="tag.color"
                >
                  #{{ tag.name }}
                </a>
                <span class="sidebar-card__tag-count">{{ tag.postCount }} posts</span>
              </li>
            }
          </ul>
        </div>
      </aside>

      <!-- Feed -->
      <div class="home-layout__feed">
        <app-feed-tabs [activeTab]="feedStore.activeTab()" (tabChange)="onTabChange($event)" />

        <app-post-feed
          [posts]="feedStore.posts()"
          [status]="feedStore.status()"
          [error]="feedStore.error()"
          [hasMore]="feedStore.hasMore()"
          [isEmpty]="feedStore.isEmpty()"
          [activeTab]="feedStore.activeTab()"
          (likeClick)="onLike($event)"
          (bookmarkClick)="onBookmark($event)"
          (loadMore)="onLoadMore()"
          (retry)="onRetry()"
        />
      </div>
    </main>
  `,
  styles: [
    `
      .home-layout {
        display: grid;
        grid-template-columns: 240px 1fr;
        gap: 24px;
        max-width: 1024px;
        margin: 0 auto;
        padding: 24px 16px;
        align-items: start;
      }

      @media (max-width: 768px) {
        .home-layout {
          grid-template-columns: 1fr;
        }
        .home-layout__sidebar {
          display: none;
        }
      }

      .home-layout__sidebar {
        position: sticky;
        top: 72px;
      }

      .sidebar-card {
        background: #ffffff;
        border: 1px solid #e5e7eb;
        border-radius: 8px;
        padding: 16px;
      }

      .sidebar-card__title {
        font-size: 11px;
        font-weight: 700;
        color: #6b7280;
        margin: 0 0 12px;
        text-transform: uppercase;
        letter-spacing: 0.07em;
      }

      .sidebar-card__list {
        list-style: none;
        padding: 0;
        margin: 0;
        display: flex;
        flex-direction: column;
        gap: 2px;
      }

      .sidebar-card__tag-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 6px 8px;
        border-radius: 4px;
        transition: background 0.1s;
      }
      .sidebar-card__tag-item:hover {
        background: #f9fafb;
      }

      .sidebar-card__tag-link {
        font-size: 13px;
        font-weight: 500;
        text-decoration: none;
      }
      .sidebar-card__tag-count {
        font-size: 11px;
        color: #9ca3af;
      }

      .home-layout__feed {
        display: flex;
        flex-direction: column;
      }
    `,
  ],
})
export class HomePageComponent implements OnInit {
  protected readonly authStore = inject(AuthStore);
  protected readonly feedStore = inject(FeedStore);
  readonly #postService = inject(PostService);
  readonly #router = inject(Router);

  protected readonly trendingTags = [
    { name: 'angular', postCount: 1420, color: '#dd0031' },
    { name: 'typescript', postCount: 980, color: '#3178c6' },
    { name: 'javascript', postCount: 3200, color: '#ca8a04' },
    { name: 'webdev', postCount: 2150, color: '#4f46e5' },
    { name: 'css', postCount: 870, color: '#264de4' },
    { name: 'rxjs', postCount: 410, color: '#b7178c' },
  ];

  ngOnInit(): void {
    this.loadFeed();
  }

  protected onTabChange(tab: FeedTab): void {
    this.feedStore.setTab(tab);
    this.loadFeed();
  }

  protected onLike(postId: string): void {
    if (!this.authStore.isAuthenticated()) {
      this.#router.navigate(['/login']);
      return;
    }
    this.feedStore.toggleLike(postId);
    const post = this.feedStore.posts().find((p) => p.id === postId);
    if (!post) return;
    const req$ = post.isLikedByMe
      ? this.#postService.unlikePost(postId)
      : this.#postService.likePost(postId);
    req$.subscribe({ error: () => this.feedStore.toggleLike(postId) });
  }

  protected onBookmark(postId: string): void {
    if (!this.authStore.isAuthenticated()) {
      this.#router.navigate(['/login']);
      return;
    }
    this.feedStore.toggleBookmark(postId);
    const post = this.feedStore.posts().find((p) => p.id === postId);
    if (!post) return;
    const req$ = post.isBookmarkedByMe
      ? this.#postService.removeBookmark(postId)
      : this.#postService.bookmarkPost(postId);
    req$.subscribe({ error: () => this.feedStore.toggleBookmark(postId) });
  }

  protected onSearch(query: string): void {
    this.#router.navigate(['/search'], { queryParams: { q: query } });
  }

  protected onLogout(): void {
    this.authStore.logout();
    this.#router.navigate(['/login']);
  }

  protected onNotificationsClick(): void {
    this.#router.navigate(['/notifications']);
  }

  protected onLoadMore(): void {
    const cursor = this.feedStore.nextCursor();
    if (!cursor) return;
    this.feedStore.setLoadingMore();
    this.#postService.getFeed(this.feedStore.activeTab(), cursor).subscribe({
      next: ({ posts, nextCursor, hasMore }) =>
        this.feedStore.appendPosts(posts, nextCursor, hasMore),
      error: () => this.feedStore.setError('Failed to load more posts.'),
    });
  }

  protected onRetry(): void {
    this.loadFeed();
  }

  private loadFeed(): void {
    this.feedStore.setLoading();
    this.#postService.getFeed(this.feedStore.activeTab()).subscribe({
      next: ({ posts, nextCursor, hasMore }) => this.feedStore.setPosts(posts, nextCursor, hasMore),
      error: () => this.feedStore.setError('Failed to load posts. Please try again.'),
    });
  }
}
