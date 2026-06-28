import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';
import { FeedTab } from '../../core/models/interfaces/post.model';

interface TabOption {
  readonly id: FeedTab;
  readonly label: string;
}

@Component({
  selector: 'app-feed-tabs',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="feed-tabs" role="tablist" aria-label="Feed filter">
      @for (tab of tabs; track tab.id) {
        <button
          type="button"
          role="tab"
          class="feed-tabs__tab"
          [class.feed-tabs__tab--active]="activeTab === tab.id"
          [attr.aria-selected]="activeTab === tab.id"
          (click)="tabChange.emit(tab.id)"
        >
          {{ tab.label }}
        </button>
      }
    </div>
  `,
  styles: [
    `
      .feed-tabs {
        display: flex;
        gap: 4px;
        border-bottom: 1px solid #e5e7eb;
        padding: 0 4px;
        overflow-x: auto;
        scrollbar-width: none;
      }

      .feed-tabs::-webkit-scrollbar {
        display: none;
      }

      .feed-tabs__tab {
        padding: 12px 16px;
        font-size: 14px;
        font-weight: 500;
        color: #6b7280;
        background: transparent;
        border: none;
        border-bottom: 2px solid transparent;
        cursor: pointer;
        white-space: nowrap;
        transition:
          color 0.12s ease,
          border-color 0.12s ease;
        position: relative;
        bottom: -1px;
      }

      .feed-tabs__tab:hover {
        color: #374151;
      }

      .feed-tabs__tab--active {
        color: #4f46e5;
        border-bottom-color: #4f46e5;
      }
    `,
  ],
})
export class FeedTabsComponent {
  @Input({ required: true }) activeTab!: FeedTab;
  @Output() tabChange = new EventEmitter<FeedTab>();

  readonly tabs: TabOption[] = [
    { id: 'for-you', label: 'For you' },
    { id: 'following', label: 'Following' },
    { id: 'latest', label: 'Latest' },
  ];
}
