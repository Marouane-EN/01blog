import { Injectable, inject, signal } from '@angular/core';
import { PostService } from '../services/post.service';
import { Post, FeedTab } from '../models/interfaces/post.model';

@Injectable({ providedIn: 'root' })
export class FeedStore {
  private postService = inject(PostService);

  // ── Private Writable Signals ──
  readonly #posts = signal<readonly Post[]>([]);
  readonly #isLoading = signal<boolean>(false);
  readonly #activeTab = signal<FeedTab>('latest');
  readonly #error = signal<string | null>(null);

  // ── Public Read-Only Signals ──
  readonly posts = this.#posts.asReadonly();
  readonly isLoading = this.#isLoading.asReadonly();
  readonly activeTab = this.#activeTab.asReadonly();
  readonly error = this.#error.asReadonly();

  // ── Actions (Mutations) ──
  loadFeed(tab: FeedTab) {
    this.#isLoading.set(true);
    this.#error.set(null);
    this.#activeTab.set(tab);

    this.postService.getFeed(tab).subscribe({
      next: (feedData) => {
        this.#posts.set(feedData.data);
        this.#isLoading.set(false);
      },
      error: (err) => {
        console.error('Error fetching feed:', err);
        this.#error.set('Failed to load posts. Please try again later.');
        this.#posts.set([]);
        this.#isLoading.set(false);
      },
    });
  }
}
