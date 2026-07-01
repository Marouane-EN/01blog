import { Component, HostListener, OnDestroy, OnInit, effect, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Subscription, forkJoin } from 'rxjs';
import { FeedStore } from '../../core/store/feed.store';
import { AuthStore } from '../../core/store/auth.store';
import { UserService } from '../../core/services/user.service';
import { PostService } from '../../core/services/post.service';
import { FeedTab, UserPreview } from '../../core/models/interfaces/post.model';
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
export class HomePageComponent implements OnInit, OnDestroy {
  // Inject the store directly to use in the HTML
  feedStore = inject(FeedStore);
  authStore = inject(AuthStore);

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private userService = inject(UserService);
  private postService = inject(PostService);
  private subscriptions = new Subscription();
  private loadedConnectionsFor: number | null = null;

  following = signal<readonly UserPreview[]>([]);
  followers = signal<readonly UserPreview[]>([]);
  connectionsLoading = signal(false);
  connectionsError = signal<string | null>(null);

  constructor() {
    effect(() => {
      const userId = this.authStore.user()?.id;

      if (!userId || this.loadedConnectionsFor === userId) {
        return;
      }

      this.loadedConnectionsFor = userId;
      this.loadConnections(userId);
    });
  }

  ngOnInit() {
    this.subscriptions.add(
      this.route.queryParamMap.subscribe((params) => {
        const query = (params.get('q') ?? '').trim();

        if (query) {
          this.feedStore.searchFeed(query);
          return;
        }

        this.feedStore.loadFeed(this.feedStore.activeTab());
      }),
    );
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
  }

  setTab(tab: FeedTab) {
    const hadSearch = !!this.feedStore.searchQuery();

    this.feedStore.loadFeed(tab);

    if (hadSearch) {
      this.router.navigate(['/']);
    }
  }

  clearSearch() {
    this.router.navigate(['/']);
  }

  togglePostLike(postId: number) {
    const post = this.feedStore.posts().find((item) => item.id === postId);

    if (!post) {
      return;
    }

    this.postService.toggleLike(postId).subscribe({
      next: (response) => {
        this.feedStore.replacePost({
          ...post,
          likedByCurrentUser: response.isLiked,
          totalLikes: response.totalLikes,
        });
      },
      error: (err) => console.error('Error toggling post like:', err),
    });
  }

  deletePost(postId: number) {
    if (!confirm('Delete this post? This cannot be undone.')) {
      return;
    }

    this.postService.deletePost(postId).subscribe({
      next: () => this.feedStore.removePost(postId),
      error: (err) => console.error('Error deleting post:', err),
    });
  }

  @HostListener('window:scroll')
  onWindowScroll() {
    const scrollPosition = window.innerHeight + window.scrollY;
    const pageHeight = document.documentElement.scrollHeight;

    if (pageHeight - scrollPosition < 480) {
      this.feedStore.loadMore();
    }
  }

  private loadConnections(userId: number) {
    this.connectionsLoading.set(true);
    this.connectionsError.set(null);

    this.subscriptions.add(
      forkJoin({
        following: this.userService.getFollowing(userId),
        followers: this.userService.getFollowers(userId),
      }).subscribe({
        next: ({ following, followers }) => {
          this.following.set(following.data);
          this.followers.set(followers.data);
          this.connectionsLoading.set(false);
        },
        error: (err) => {
          console.error('Error fetching user connections:', err);
          this.following.set([]);
          this.followers.set([]);
          this.connectionsError.set('Could not load people right now.');
          this.connectionsLoading.set(false);
        },
      }),
    );
  }
}
