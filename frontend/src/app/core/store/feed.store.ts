import { Injectable, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { PostService } from '../services/post.service';
import { UserService } from '../services/user.service';
import { Post, FeedTab, PostSearchResult, UserDto } from '../models';
import { ToastService } from '../services/toast.service';
import { extractErrorMessage } from '../utils/http-error.utils';

@Injectable({ providedIn: 'root' })
export class FeedStore {
  private postService = inject(PostService);
  private userService = inject(UserService);
  private toastService = inject(ToastService);

  // ── Private Writable Signals ──
  readonly #posts = signal<readonly Post[]>([]);
  readonly #searchResults = signal<readonly PostSearchResult[]>([]);
  readonly #userSearchResults = signal<readonly UserDto[]>([]);
  readonly #isLoading = signal<boolean>(false);
  readonly #isLoadingMore = signal<boolean>(false);
  readonly #activeTab = signal<FeedTab>('latest');
  readonly #error = signal<string | null>(null);
  readonly #nextCursor = signal<number | null>(null);
  readonly #nextUserCursor = signal<number | null>(null);
  readonly #hasMore = signal<boolean>(false);
  readonly #hasMoreUsers = signal<boolean>(false);
  readonly #searchQuery = signal<string>('');

  // ── Public Read-Only Signals ──
  readonly posts = this.#posts.asReadonly();
  readonly searchResults = this.#searchResults.asReadonly();
  readonly userSearchResults = this.#userSearchResults.asReadonly();
  readonly isLoading = this.#isLoading.asReadonly();
  readonly isLoadingMore = this.#isLoadingMore.asReadonly();
  readonly activeTab = this.#activeTab.asReadonly();
  readonly error = this.#error.asReadonly();
  readonly hasMore = this.#hasMore.asReadonly();
  readonly searchQuery = this.#searchQuery.asReadonly();

  // ── Actions (Mutations) ──
  loadFeed(tab: FeedTab) {
    this.#isLoading.set(true);
    this.#isLoadingMore.set(false);
    this.#error.set(null);
    this.#activeTab.set(tab);
    this.#searchQuery.set('');
    this.#searchResults.set([]);
    this.#userSearchResults.set([]);
    this.#nextCursor.set(null);
    this.#hasMore.set(false);

    this.postService.getFeed(tab).subscribe({
      next: (feedData) => {
        this.#posts.set(feedData.data);
        this.#searchResults.set([]);
        this.#userSearchResults.set([]);
        this.#nextCursor.set(feedData.nextCursor);
        this.#hasMore.set(feedData.hasMore);
        this.#isLoading.set(false);
      },
      error: (err) => {
        const message = extractErrorMessage(err, 'Failed to load posts. Please try again later.');
        this.#error.set(message);
        this.#posts.set([]);
        this.#searchResults.set([]);
        this.#userSearchResults.set([]);
        this.#nextCursor.set(null);
        this.#hasMore.set(false);
        this.#isLoading.set(false);
        this.toastService.error(message);
      },
    });
  }

  searchFeed(query: string) {
    const normalizedQuery = query.trim();

    if (!normalizedQuery) {
      this.loadFeed(this.#activeTab());
      return;
    }

    this.#isLoading.set(true);
    this.#isLoadingMore.set(false);
    this.#error.set(null);
    this.#searchQuery.set(normalizedQuery);
    this.#nextCursor.set(null);
    this.#nextUserCursor.set(null);
    this.#hasMore.set(false);
    this.#hasMoreUsers.set(false);
    this.#posts.set([]);

    forkJoin({
      posts: this.postService.searchPosts(normalizedQuery),
      users: this.userService.searchUsers(normalizedQuery),
    }).subscribe({
      next: ({ posts, users }) => {
        this.#searchResults.set(posts.data);
        this.#userSearchResults.set(users.data);
        this.#nextCursor.set(posts.nextCursor);
        this.#nextUserCursor.set(users.nextCursor);
        this.#hasMore.set(posts.hasMore);
        this.#hasMoreUsers.set(users.hasMore);
        this.#isLoading.set(false);
      },
      error: (err) => {
        const message = extractErrorMessage(err, 'Failed to search posts. Please try again later.');
        this.#error.set(message);
        this.#searchResults.set([]);
        this.#userSearchResults.set([]);
        this.#nextCursor.set(null);
        this.#nextUserCursor.set(null);
        this.#hasMore.set(false);
        this.#hasMoreUsers.set(false);
        this.#isLoading.set(false);
        this.toastService.error(message);
      },
    });
  }

  loadMore() {
    const cursor = this.#nextCursor();

    if (
      (!this.#searchQuery() && (!this.#hasMore() || !cursor)) ||
      this.#isLoading() ||
      this.#isLoadingMore()
    ) {
      return;
    }

    this.#isLoadingMore.set(true);
    this.#error.set(null);

    if (this.#searchQuery()) {
      if (!this.#hasMore() && !this.#hasMoreUsers()) {
        this.#isLoadingMore.set(false);
        return;
      }

      forkJoin({
        posts: this.#hasMore()
          ? this.postService.searchPosts(this.#searchQuery(), this.#nextCursor())
          : this.postService.searchPosts(this.#searchQuery(), null),
        users: this.#hasMoreUsers()
          ? this.userService.searchUsers(this.#searchQuery(), this.#nextUserCursor())
          : this.userService.searchUsers(this.#searchQuery(), null),
      }).subscribe({
        next: ({ posts, users }) => {
          if (this.#hasMore()) {
            this.#searchResults.update((results) => [...results, ...posts.data]);
          }
          if (this.#hasMoreUsers()) {
            this.#userSearchResults.update((results) => [...results, ...users.data]);
          }
          this.#nextCursor.set(posts.nextCursor);
          this.#nextUserCursor.set(users.nextCursor);
          this.#hasMore.set(posts.hasMore);
          this.#hasMoreUsers.set(users.hasMore);
          this.#isLoadingMore.set(false);
        },
        error: (err) => {
          const message = extractErrorMessage(
            err,
            'Failed to load more search results. Please try again later.',
          );
          this.#error.set(message);
          this.#isLoadingMore.set(false);
          this.toastService.error(message);
        },
      });
      return;
    }

    this.postService.getFeed(this.#activeTab(), cursor).subscribe({
      next: (feedData) => {
        this.#posts.update((posts) => [...posts, ...feedData.data]);
        this.#nextCursor.set(feedData.nextCursor);
        this.#hasMore.set(feedData.hasMore);
        this.#isLoadingMore.set(false);
      },
      error: (err) => {
        const message = extractErrorMessage(err, 'Failed to load more posts. Please try again later.');
        this.#error.set(message);
        this.#isLoadingMore.set(false);
        this.toastService.error(message);
      },
    });
  }

  replacePost(updatedPost: Post) {
    this.#posts.update((posts) => posts.map((post) => (post.id === updatedPost.id ? updatedPost : post)));
  }

  removePost(postId: number) {
    this.#posts.update((posts) => posts.filter((post) => post.id !== postId));
  }
}
