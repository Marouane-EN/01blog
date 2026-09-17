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
import { TagService } from '../../core/services/tag.service';
import { ToastService } from '../../core/services/toast.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { extractErrorMessage } from '../../core/utils/http-error.utils';
import { FeedTab, PopularUser, PublicProfile, TagTrend, UserDto } from '../../core/models/index';
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
  private tagService = inject(TagService);
  private toastService = inject(ToastService);
  private confirmDialog = inject(ConfirmDialogService);
  private subscriptions = new Subscription();
  private loadedConnectionsFor: number | null = null;
  following = signal<readonly UserDto[]>([]);
  followers = signal<readonly UserDto[]>([]);
  profile = signal<PublicProfile | null>(null);
  connectionsLoading = signal(false);
  connectionsError = signal<string | null>(null);
  trendingTags = signal<readonly TagTrend[]>([]);
  popularUsers = signal<readonly PopularUser[]>([]);

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

    this.tagService.getTrendingTags(5).subscribe({
      next: (tags) => this.trendingTags.set(tags),
      error: (err) => {
        this.toastService.error(extractErrorMessage(err, 'Could not load trending tags.'));
      },
    });

    this.userService.getPopularUsers(5).subscribe({
      next: (users) => this.popularUsers.set(users),
      error: (err) => {
        this.toastService.error(extractErrorMessage(err, 'Could not load popular users.'));
      },
    });
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
      error: (err) => {
        this.toastService.error(extractErrorMessage(err, 'Could not update like status.'));
      },
    });
  }

  async deletePost(postId: number) {
    const confirmed = await this.confirmDialog.confirm({
      title: 'Delete this post?',
      description: 'This post will be permanently removed. This cannot be undone.',
      confirmLabel: 'Delete post',
    });

    if (!confirmed) {
      return;
    }

    this.postService.deletePost(postId).subscribe({
      next: () => {
        this.feedStore.removePost(postId);
        this.toastService.success('Post deleted.');
      },
      error: (err) => {
        this.toastService.error(extractErrorMessage(err, 'Could not delete this post.'));
      },
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
        profile: this.userService.getProfile(this.authStore.user()?.username ?? ''),
        following: this.userService.getFollowing(userId),
        followers: this.userService.getFollowers(userId),
      }).subscribe({
        next: ({ profile, following, followers }) => {
          this.profile.set(profile);
          this.following.set(following.data);
          this.followers.set(followers.data);
          this.connectionsLoading.set(false);
        },
        error: (err) => {
          const message = extractErrorMessage(err, 'Could not load people right now.');
          this.profile.set(null);
          this.following.set([]);
          this.followers.set([]);
          this.connectionsError.set(message);
          this.connectionsLoading.set(false);
          this.toastService.error(message);
        },
      }),
    );
  }
}
