import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnInit, effect, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../core/services/auth.service';
import { PostService } from '../../core/services/post.service';
import { UserService } from '../../core/services/user.service';
import { AuthStore } from '../../core/store/auth.store';
import { Post, PublicProfile, UserDto } from '../../core/models';
import { PostCardComponent } from '../../shared/components/post-card/post-card.component';
import { ReportDialogService } from '../../shared/services/report-dialog.service';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    RouterModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    PostCardComponent,
  ],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss'],
})
export class ProfileComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private userService = inject(UserService);
  private postService = inject(PostService);
  private authService = inject(AuthService);
  private reportDialog = inject(ReportDialogService);
  private toastService = inject(ToastService);
  authStore = inject(AuthStore);

  private requestedUsername = signal<string | null>(null);
  private loadedUsername: string | null = null;

  private router = inject(Router);
  profile = signal<PublicProfile | null>(null);
  posts = signal<readonly Post[]>([]);
  nextCursor = signal<number | null>(null);
  hasMorePosts = signal(false);
  isLoading = signal(true);
  postsLoading = signal(false);
  avatarUploading = signal(false);
  followLoading = signal(false);
  isFollowing = signal(false);
  errorMessage = signal('');
  connectionPanel = signal<'followers' | 'following' | null>(null);
  connectionUsers = signal<readonly UserDto[]>([]);
  connectionCursor = signal<number | null>(null);
  connectionHasMore = signal(false);
  connectionsLoading = signal(false);

  constructor() {
    effect(() => {
      const username = this.requestedUsername() || this.authStore.displayName();

      if (!username || username === this.loadedUsername) {
        return;
      }

      this.loadedUsername = username;
      this.loadProfile(username);
    });
  }

  ngOnInit() {
    this.route.paramMap.subscribe((params) => {
      this.loadedUsername = null;
      this.requestedUsername.set(params.get('username'));
    });
  }

  get isOwnProfile() {
    return this.profile()?.username === this.authStore.displayName();
  }

  onAvatarSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];

    if (!file || !this.isOwnProfile) {
      return;
    }

    this.avatarUploading.set(true);
    this.errorMessage.set('');

    this.userService.uploadAvatar(file).subscribe({
      next: () => {
        this.authService.fetchMe().subscribe();
        const username = this.profile()?.username;

        if (username) {
          this.loadProfile(username);
        }

        this.avatarUploading.set(false);
      },
      error: (err) => {
        console.error('Error uploading avatar:', err);
        this.errorMessage.set(err.error || 'Could not update your profile picture.');
        this.avatarUploading.set(false);
      },
    });
  }

  toggleFollow() {
    // 1. Check if the user is logged in first!
    if (!this.authStore.isAuthenticated()) {
      // If not, redirect them to the login page immediately.
      // Make sure to inject the Router in your component constructor or via inject(Router)!
      this.router.navigate(['/login']);
      return;
    }

    const profile = this.profile();

    if (!profile || this.isOwnProfile || this.followLoading()) {
      return;
    }

    this.followLoading.set(true);
    this.userService.toggleSubscription(profile.username).subscribe({
      next: (response) => {
        this.isFollowing.set(response.isSubscribed);
        this.profile.set({
          ...profile,
          followersCount: Number(response.totalSubscribers),
        });
        this.followLoading.set(false);
        this.toastService.success(
          response.isSubscribed
            ? `You are now following ${profile.username}.`
            : `You unfollowed ${profile.username}.`,
        );
      },
      error: (err) => {
        console.error('Error toggling follow:', err);
        this.errorMessage.set(err.error || 'Could not update follow status.');
        this.followLoading.set(false);
        this.toastService.error(err.error || 'Could not update follow status.');
      },
    });
  }

  reportProfile() {
    const profile = this.profile();

    if (!profile || this.isOwnProfile) {
      return;
    }

    this.reportDialog.open({
      targetType: 'user',
      targetId: profile.id,
      label: `@${profile.username}`,
    });
  }

  openConnections(type: 'followers' | 'following') {
    const profile = this.profile();

    if (!profile) {
      return;
    }

    this.connectionPanel.set(type);
    this.connectionUsers.set([]);
    this.connectionCursor.set(null);
    this.connectionHasMore.set(false);
    this.loadConnections(type, profile.id);
  }

  closeConnections() {
    this.connectionPanel.set(null);
  }

  loadMoreConnections() {
    const profile = this.profile();
    const type = this.connectionPanel();

    if (!profile || !type || !this.connectionHasMore() || this.connectionsLoading()) {
      return;
    }

    this.loadConnections(type, profile.id, this.connectionCursor());
  }

  loadMorePosts() {
    const profile = this.profile();

    if (!profile || !this.hasMorePosts() || this.postsLoading()) {
      return;
    }

    this.loadPosts(profile.id, this.nextCursor());
  }

  togglePostLike(postId: number) {
    const post = this.posts().find((item) => item.id === postId);

    if (!post) {
      return;
    }

    this.postService.toggleLike(postId).subscribe({
      next: (response) => {
        this.posts.update((posts) =>
          posts.map((item) =>
            item.id === postId
              ? { ...item, likedByCurrentUser: response.isLiked, totalLikes: response.totalLikes }
              : item,
          ),
        );
      },
      error: (err) => console.error('Error toggling post like:', err),
    });
  }

  deletePost(postId: number) {
    if (!confirm('Delete this post? This cannot be undone.')) {
      return;
    }

    this.postService.deletePost(postId).subscribe({
      next: () => {
        this.posts.update((posts) => posts.filter((post) => post.id !== postId));
        const profile = this.profile();

        if (profile) {
          this.profile.set({
            ...profile,
            postsCount: Math.max(0, profile.postsCount - 1),
          });
        }

        this.toastService.success('Post deleted.');
      },
      error: (err) => {
        console.error('Error deleting post:', err);
        this.toastService.error(err.error || 'Could not delete this post.');
      },
    });
  }

  private loadProfile(username: string) {
    this.isLoading.set(true);
    this.errorMessage.set('');
    this.profile.set(null);
    this.posts.set([]);
    this.nextCursor.set(null);
    this.hasMorePosts.set(false);

    this.userService.getProfile(username).subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.loadFollowState(profile);
        this.isLoading.set(false);
        this.loadPosts(profile.id);
      },
      error: (err) => {
        console.error('Error loading profile:', err);
        this.errorMessage.set('Could not load this profile.');
        this.isLoading.set(false);
      },
    });
  }

  private loadPosts(userId: number, cursor?: number | null) {
    this.postsLoading.set(true);

    this.postService.getPostsByUser(userId, cursor).subscribe({
      next: (response) => {
        this.posts.update((posts) => (cursor ? [...posts, ...response.data] : response.data));
        this.nextCursor.set(response.nextCursor);
        this.hasMorePosts.set(response.hasMore);
        this.postsLoading.set(false);
      },
      error: (err) => {
        console.error('Error loading profile posts:', err);
        this.postsLoading.set(false);
      },
    });
  }

  private loadFollowState(profile: PublicProfile) {
    const currentUser = this.authStore.user();

    this.isFollowing.set(false);

    if (!currentUser || currentUser.username === profile.username) {
      return;
    }

    this.userService.getFollowing(currentUser.id).subscribe({
      next: (response) => {
        this.isFollowing.set(response.data.some((user) => user.username === profile.username));
      },
      error: (err) => console.error('Error loading follow state:', err),
    });
  }

  private loadConnections(type: 'followers' | 'following', userId: number, cursor?: number | null) {
    this.connectionsLoading.set(true);

    const request =
      type === 'followers'
        ? this.userService.getFollowers(userId, cursor)
        : this.userService.getFollowing(userId, cursor);

    request.subscribe({
      next: (response) => {
        this.connectionUsers.update((users) =>
          cursor ? [...users, ...response.data] : response.data,
        );
        this.connectionCursor.set(response.nextCursor);
        this.connectionHasMore.set(response.hasMore);
        this.connectionsLoading.set(false);
      },
      error: (err) => {
        console.error('Error loading connections:', err);
        this.connectionsLoading.set(false);
      },
    });
  }
}
