import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { FeedStore } from '../../core/store/feed.store';
import { FeedTab } from '../../core/models/interfaces/post.model';
import { PostCardComponent } from '../../shared/components/post-card/post-card.component';

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    PostCardComponent,
  ],
  templateUrl: './home-page.component.html',
  styleUrls: ['./home-page.component.scss'],
})
export class HomePageComponent implements OnInit {
  // Inject the store directly to use in the HTML
  feedStore = inject(FeedStore);

  ngOnInit() {
    // Load the default feed when the page loads
    this.feedStore.loadFeed('latest');
  }

  setTab(tab: FeedTab) {
    // Tell the store to fetch the new data
    this.feedStore.loadFeed(tab);
  }
}
