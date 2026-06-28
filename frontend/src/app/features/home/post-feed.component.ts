import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';
import { FeedStatus } from '../../core/store/feed.store';
import { FeedTab, Post, Tag } from '../../core/models/interfaces/post.model';
import { PostCardComponent } from '../../shared/components/post-card/post-card.component';

@Component({
  selector: 'app-post-feed',
  standalone: true,
  imports: [PostCardComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="post-feed" [attr.aria-busy]="status === 'loading' || status === 'loadingMore'">
      @if (status === 'loading') {
        <div class="post-feed__state" role="status">Loading posts...</div>
      } @else if (status === 'error') {
        <div class="post-feed__state post-feed__state--error" role="alert">
          <p>{{ error || 'Something went wrong while loading posts.' }}</p>
          <button type="button" class="post-feed__button" (click)="retry.emit()">Try again</button>
        </div>
      } @else if (isEmpty) {
        <div class="post-feed__state">No posts yet.</div>
      } @else {
        <div class="post-feed__list">
          @for (post of posts; track post.id) {
            <app-post-card
              [post]="post"
              (likeClick)="likeClick.emit($event)"
              (bookmarkClick)="bookmarkClick.emit($event)"
              (tagClick)="tagClick.emit($event)"
            />
          }
        </div>
      }

      @if (hasMore && status !== 'loading' && status !== 'error') {
        <button
          type="button"
          class="post-feed__button post-feed__button--load-more"
          [disabled]="status === 'loadingMore'"
          (click)="loadMore.emit()"
        >
          {{ status === 'loadingMore' ? 'Loading...' : 'Load more' }}
        </button>
      }
    </section>
  `,
  styles: [
    `
      :host {
        display: block;
      }

      .post-feed {
        display: flex;
        flex-direction: column;
        gap: 16px;
      }

      .post-feed__list {
        display: flex;
        flex-direction: column;
        gap: 16px;
      }

      .post-feed__state {
        padding: 32px 16px;
        border: 1px solid #e5e7eb;
        border-radius: 8px;
        background: #ffffff;
        font-size: 14px;
        color: #6b7280;
        text-align: center;
      }

      .post-feed__state--error {
        color: #b91c1c;
      }

      .post-feed__state p {
        margin: 0 0 12px;
      }

      .post-feed__button {
        align-self: center;
        min-height: 40px;
        padding: 0 16px;
        border: 1px solid #d1d5db;
        border-radius: 6px;
        background: #ffffff;
        color: #374151;
        font-size: 14px;
        font-weight: 600;
        cursor: pointer;
      }

      .post-feed__button:hover:not(:disabled) {
        background: #f9fafb;
      }

      .post-feed__button:disabled {
        cursor: wait;
        opacity: 0.7;
      }

      .post-feed__button--load-more {
        width: 100%;
      }
    `,
  ],
})
export class PostFeedComponent {
  @Input() posts: readonly Post[] = [];
  @Input() status: FeedStatus = 'idle';
  @Input() error: string | null = null;
  @Input() hasMore = false;
  @Input() isEmpty = false;
  @Input({ required: true }) activeTab!: FeedTab;

  @Output() likeClick = new EventEmitter<string>();
  @Output() bookmarkClick = new EventEmitter<string>();
  @Output() tagClick = new EventEmitter<Tag>();
  @Output() loadMore = new EventEmitter<void>();
  @Output() retry = new EventEmitter<void>();
}
