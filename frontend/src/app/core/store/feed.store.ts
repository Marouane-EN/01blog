import { Injectable, Signal, computed, signal } from '@angular/core';
import { FeedTab, Post } from '../models/interfaces/post.model';

export type FeedStatus = 'idle' | 'loading' | 'loadingMore' | 'loaded' | 'error';

interface FeedState {
  readonly posts: readonly Post[];
  readonly status: FeedStatus;
  readonly error: string | null;
  readonly nextCursor: string | null;
  readonly hasMore: boolean;
  readonly activeTab: FeedTab;
}

const INITIAL_STATE: FeedState = {
  posts: [],
  status: 'idle',
  error: null,
  nextCursor: null,
  hasMore: false,
  activeTab: 'for-you',
};

/**
 * Signal-first post feed store.
 * Holds paginated post lists and the active feed tab.
 */
@Injectable({ providedIn: 'root' })
export class FeedStore {
  readonly #state = signal<FeedState>(INITIAL_STATE);

  // ── Public derived signals ─────────────────────────────────────────────
  readonly posts: Signal<readonly Post[]> = computed(() => this.#state().posts);
  readonly status: Signal<FeedStatus> = computed(() => this.#state().status);
  readonly error: Signal<string | null> = computed(() => this.#state().error);
  readonly hasMore: Signal<boolean> = computed(() => this.#state().hasMore);
  readonly nextCursor: Signal<string | null> = computed(() => this.#state().nextCursor);
  readonly activeTab: Signal<FeedTab> = computed(() => this.#state().activeTab);
  readonly isLoading: Signal<boolean> = computed(
    () => this.#state().status === 'loading' || this.#state().status === 'loadingMore'
  );
  readonly isEmpty: Signal<boolean> = computed(
    () => this.#state().status === 'loaded' && this.#state().posts.length === 0
  );

  // ── Mutations ──────────────────────────────────────────────────────────
  setLoading(): void {
    this.#state.update(s => ({ ...s, status: 'loading', posts: [], nextCursor: null }));
  }

  setLoadingMore(): void {
    this.#state.update(s => ({ ...s, status: 'loadingMore' }));
  }

  setPosts(posts: readonly Post[], nextCursor: string | null, hasMore: boolean): void {
    this.#state.update(s => ({ ...s, posts, nextCursor, hasMore, status: 'loaded', error: null }));
  }

  appendPosts(posts: readonly Post[], nextCursor: string | null, hasMore: boolean): void {
    this.#state.update(s => ({
      ...s,
      posts: [...s.posts, ...posts],
      nextCursor,
      hasMore,
      status: 'loaded',
      error: null,
    }));
  }

  setError(error: string): void {
    this.#state.update(s => ({ ...s, status: 'error', error }));
  }

  setTab(tab: FeedTab): void {
    this.#state.update(s => ({ ...s, activeTab: tab }));
  }

  toggleLike(postId: string): void {
    this.#state.update(s => ({
      ...s,
      posts: s.posts.map(p =>
        p.id === postId
          ? {
              ...p,
              isLikedByMe: !p.isLikedByMe,
              likesCount: p.isLikedByMe ? p.likesCount - 1 : p.likesCount + 1,
            }
          : p
      ),
    }));
  }

  toggleBookmark(postId: string): void {
    this.#state.update(s => ({
      ...s,
      posts: s.posts.map(p =>
        p.id === postId ? { ...p, isBookmarkedByMe: !p.isBookmarkedByMe } : p
      ),
    }));
  }
}
