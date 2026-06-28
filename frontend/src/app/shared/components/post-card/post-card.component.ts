import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { Post, Tag } from '../../../core/models/interfaces/post.model';
import { AvatarComponent } from '../avatar/avatar.component';
import { TagBadgeComponent } from '../tag-badge/tag-badge.component';

@Component({
  selector: 'app-post-card',
  standalone: true,
  imports: [RouterLink, DatePipe, AvatarComponent, TagBadgeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <article class="post-card" [attr.aria-label]="post.title">
      <!-- Author row -->
      <div class="post-card__author">
        <a
          [routerLink]="['/profile', post.author.username]"
          class="post-card__author-link"
          [attr.aria-label]="'View ' + post.author.displayName + ' profile'"
        >
          <app-avatar
            [displayName]="post.author.displayName"
            [src]="post.author.avatarUrl"
            size="sm"
          />
          <div class="post-card__author-meta">
            <span class="post-card__author-name">{{ post.author.displayName }}</span>
            <span class="post-card__date">{{ post.publishedAt | date: 'MMM d' }}</span>
          </div>
        </a>
      </div>

      <!-- Cover image (optional) -->
      @if (post.coverImageUrl) {
        <a [routerLink]="['/posts', post.slug]" class="post-card__cover-link">
          <img
            [src]="post.coverImageUrl"
            [alt]="post.title"
            class="post-card__cover"
            loading="lazy"
          />
        </a>
      }

      <!-- Body -->
      <div class="post-card__body">
        <a [routerLink]="['/posts', post.slug]" class="post-card__title-link">
          <h2 class="post-card__title">{{ post.title }}</h2>
        </a>

        <!-- Tags -->
        @if (post.tags.length > 0) {
          <div class="post-card__tags" role="list" aria-label="Post tags">
            @for (tag of post.tags; track tag.id) {
              <div role="listitem">
                <app-tag-badge [tag]="tag" (tagClick)="tagClick.emit($event)" />
              </div>
            }
          </div>
        }
      </div>

      <!-- Footer: reading time + engagement -->
      <div class="post-card__footer">
        <div class="post-card__actions">
          <!-- Like button -->
          <button
            type="button"
            class="post-card__action-btn"
            [class.post-card__action-btn--active]="post.isLikedByMe"
            (click)="likeClick.emit(post.id)"
            [attr.aria-label]="
              (post.isLikedByMe ? 'Unlike' : 'Like') + ' — ' + post.likesCount + ' likes'
            "
            [attr.aria-pressed]="post.isLikedByMe"
          >
            <svg
              width="18"
              height="18"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
              aria-hidden="true"
            >
              <path
                d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78L12 21.23l8.84-8.84a5.5 5.5 0 0 0 0-7.78z"
              />
            </svg>
            <span>{{ post.likesCount }} likes</span>
          </button>

          <!-- Comment link -->
          <a
            [routerLink]="['/posts', post.slug]"
            fragment="comments"
            class="post-card__action-btn"
            [attr.aria-label]="post.commentsCount + ' comments'"
          >
            <svg
              width="18"
              height="18"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
              aria-hidden="true"
            >
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
            </svg>
            <span>{{ post.commentsCount }} comments</span>
          </a>
        </div>

        <div class="post-card__right">
          <!-- Bookmark button -->
          <button
            type="button"
            class="post-card__bookmark-btn"
            [class.post-card__bookmark-btn--active]="post.isBookmarkedByMe"
            (click)="bookmarkClick.emit(post.id)"
            [attr.aria-label]="(post.isBookmarkedByMe ? 'Remove bookmark' : 'Bookmark') + ' post'"
            [attr.aria-pressed]="post.isBookmarkedByMe"
          >
            <svg
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
              <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
            </svg>
          </button>

          <span class="post-card__reading-time">{{ post.readingTimeMinutes }} min read</span>
        </div>
      </div>
    </article>
  `,
  styles: [
    `
      :host {
        display: block;
      }

      .post-card {
        background: #ffffff;
        border: 1px solid #e5e7eb;
        border-radius: 8px;
        overflow: hidden;
        transition:
          box-shadow 0.15s ease,
          border-color 0.15s ease;
      }

      .post-card:hover {
        border-color: #d1d5db;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      }

      /* Author row */
      .post-card__author {
        padding: 16px 16px 0;
      }

      .post-card__author-link {
        display: flex;
        align-items: center;
        gap: 10px;
        text-decoration: none;
        color: inherit;
        width: fit-content;
      }

      .post-card__author-meta {
        display: flex;
        flex-direction: column;
      }

      .post-card__author-name {
        font-size: 14px;
        font-weight: 600;
        color: #111827;
        line-height: 1.2;
      }

      .post-card__author-name:hover {
        color: #4f46e5;
      }

      .post-card__date {
        font-size: 12px;
        color: #9ca3af;
        margin-top: 2px;
      }

      /* Cover image */
      .post-card__cover-link {
        display: block;
        margin-top: 12px;
      }

      .post-card__cover {
        width: 100%;
        height: 200px;
        object-fit: cover;
      }

      /* Body */
      .post-card__body {
        padding: 12px 16px 8px;
      }

      .post-card__title-link {
        text-decoration: none;
        color: inherit;
      }

      .post-card__title {
        font-size: 20px;
        font-weight: 700;
        color: #111827;
        margin: 0 0 10px;
        line-height: 1.3;
        transition: color 0.1s ease;
      }

      .post-card__title-link:hover .post-card__title {
        color: #4f46e5;
      }

      /* Tags */
      .post-card__tags {
        display: flex;
        flex-wrap: wrap;
        gap: 4px;
      }

      /* Footer */
      .post-card__footer {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 8px 12px 12px;
      }

      .post-card__actions {
        display: flex;
        align-items: center;
        gap: 4px;
      }

      .post-card__action-btn {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 6px 10px;
        border-radius: 6px;
        border: none;
        background: transparent;
        font-size: 13px;
        color: #6b7280;
        cursor: pointer;
        text-decoration: none;
        transition:
          background 0.12s ease,
          color 0.12s ease;
      }

      .post-card__action-btn:hover {
        background: #f3f4f6;
        color: #111827;
      }

      .post-card__action-btn--active {
        color: #e11d48;
      }

      .post-card__action-btn--active:hover {
        background: #fff1f2;
      }

      .post-card__right {
        display: flex;
        align-items: center;
        gap: 8px;
      }

      .post-card__bookmark-btn {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 32px;
        height: 32px;
        border-radius: 6px;
        border: none;
        background: transparent;
        color: #9ca3af;
        cursor: pointer;
        transition:
          background 0.12s ease,
          color 0.12s ease;
      }

      .post-card__bookmark-btn:hover {
        background: #f3f4f6;
        color: #4f46e5;
      }

      .post-card__bookmark-btn--active {
        color: #4f46e5;
      }

      .post-card__reading-time {
        font-size: 12px;
        color: #9ca3af;
        white-space: nowrap;
      }
    `,
  ],
})
export class PostCardComponent {
  @Input({ required: true }) post!: Post;

  @Output() likeClick = new EventEmitter<string>();
  @Output() bookmarkClick = new EventEmitter<string>();
  @Output() tagClick = new EventEmitter<Tag>();
}
