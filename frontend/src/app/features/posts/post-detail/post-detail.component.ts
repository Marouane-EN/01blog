import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { forkJoin } from 'rxjs';
import { Comment, Post } from '../../../core/models';
import { CommentService } from '../../../core/services/comment.service';
import { PostService } from '../../../core/services/post.service';
import { AuthStore } from '../../../core/store/auth.store';
import { ReportDialogService } from '../../../shared/services/report-dialog.service';
import { ToastService } from '../../../core/services/toast.service';
import {
  MediaCarouselComponent,
  MediaCarouselItem,
} from '../../../shared/components/media-carousel/media-carousel.component';

@Component({
  selector: 'app-post-detail',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    ReactiveFormsModule,
    RouterModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MediaCarouselComponent,
  ],
  templateUrl: './post-detail.component.html',
  styleUrls: ['./post-detail.component.scss'],
})
export class PostDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private postService = inject(PostService);
  private commentService = inject(CommentService);
  private reportDialog = inject(ReportDialogService);
  private toastService = inject(ToastService);
  authStore = inject(AuthStore);

  post = signal<Post | null>(null);
  comments = signal<readonly Comment[]>([]);
  commentsCursor = signal<number | null>(null);
  hasMoreComments = signal(false);
  isLoading = signal(true);
  commentsLoading = signal(false);
  isSubmittingComment = signal(false);
  isEditingPost = signal(false);
  isSavingPost = signal(false);
  editingCommentId = signal<number | null>(null);
  savingCommentId = signal<number | null>(null);
  isUploadingMedia = signal(false);
  errorMessage = signal('');

  commentForm = this.fb.nonNullable.group({
    content: ['', [Validators.required, Validators.maxLength(200)]],
  });

  postEditForm = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(128)]],
    content: ['', [Validators.required, Validators.maxLength(1000)]],
    tags: [''],
  });

  commentEditForm = this.fb.nonNullable.group({
    content: ['', [Validators.required, Validators.maxLength(200)]],
  });

  ngOnInit() {
    const slug = this.route.snapshot.paramMap.get('slug');

    if (!slug) {
      this.errorMessage.set('Post not found.');
      this.isLoading.set(false);
      return;
    }

    this.postService.getPostBySlug(slug).subscribe({
      next: (post) => {
        this.post.set(post);
        this.patchPostEditForm(post);
        this.isLoading.set(false);
        this.loadComments(post.id);

        if (this.route.snapshot.queryParamMap.get('edit') === 'true' && this.isOwnPost(post)) {
          this.isEditingPost.set(true);
        }
      },
      error: (err) => {
        console.error('Error loading post:', err);
        this.errorMessage.set('Could not load this post.');
        this.isLoading.set(false);
      },
    });
  }

  isOwnPost(post = this.post()) {
    return !!post && this.authStore.user()?.id === post.author.id;
  }

  startPostEdit() {
    const post = this.post();

    if (!post || !this.isOwnPost(post)) {
      return;
    }

    this.patchPostEditForm(post);
    this.isEditingPost.set(true);
  }

  cancelPostEdit() {
    const post = this.post();

    if (post) {
      this.patchPostEditForm(post);
    }

    this.isEditingPost.set(false);
  }

  savePostEdit() {
    const post = this.post();

    if (!post || this.postEditForm.invalid || this.isSavingPost()) {
      this.postEditForm.markAllAsTouched();
      return;
    }

    const value = this.postEditForm.getRawValue();
    const tags = value.tags
      .split(',')
      .map((tag) => tag.trim())
      .filter(Boolean)
      .slice(0, 5);

    this.isSavingPost.set(true);
    this.postService
      .updatePost(post.id, {
        title: value.title.trim(),
        content: value.content.trim(),
        tags,
      })
      .subscribe({
        next: (updatedPost) => {
          this.post.set(updatedPost);
          this.patchPostEditForm(updatedPost);
          this.isEditingPost.set(false);
          this.isSavingPost.set(false);
          this.toastService.success('Post updated.');
        },
        error: (err) => {
          console.error('Error updating post:', err);
          this.errorMessage.set(err.error || 'Could not update this post.');
          this.isSavingPost.set(false);
          this.toastService.error(err.error || 'Could not update this post.');
        },
      });
  }

  deletePost() {
    const post = this.post();

    if (!post || !this.isOwnPost(post) || !confirm('Delete this post? This cannot be undone.')) {
      return;
    }

    this.postService.deletePost(post.id).subscribe({
      next: () => {
        this.toastService.success('Post deleted.');
        this.router.navigate(['/']);
      },
      error: (err) => {
        console.error('Error deleting post:', err);
        this.errorMessage.set(err.error || 'Could not delete this post.');
        this.toastService.error(err.error || 'Could not delete this post.');
      },
    });
  }

  togglePostLike() {
    const post = this.post();

    if (!post) {
      return;
    }

    this.postService.toggleLike(post.id).subscribe({
      next: (response) => {
        this.post.set({
          ...post,
          likedByCurrentUser: response.isLiked,
          totalLikes: response.totalLikes,
        });
      },
      error: (err) => console.error('Error toggling post like:', err),
    });
  }

  scrollToComments() {
    document.getElementById('comments')?.scrollIntoView({
      behavior: 'smooth',
      block: 'start',
    });
  }

  submitComment() {
    const post = this.post();

    if (!post || this.commentForm.invalid || this.isSubmittingComment()) {
      this.commentForm.markAllAsTouched();
      return;
    }

    this.isSubmittingComment.set(true);

    this.commentService
      .createComment(post.id, {
        content: this.commentForm.controls.content.value.trim(),
      })
      .subscribe({
        next: (comment) => {
          this.comments.update((comments) => [comment, ...comments]);
          this.commentForm.reset();
          this.isSubmittingComment.set(false);
        },
        error: (err) => {
          console.error('Error creating comment:', err);
          this.errorMessage.set(err.error || 'Could not add your comment.');
          this.isSubmittingComment.set(false);
        },
      });
  }

  loadMoreComments() {
    const post = this.post();

    if (post && this.hasMoreComments() && !this.commentsLoading()) {
      this.loadComments(post.id, this.commentsCursor());
    }
  }

  isOwnComment(comment: Comment) {
    return this.authStore.user()?.id === comment.author.id;
  }

  reportPost() {
    const post = this.post();

    if (!post) {
      return;
    }

    this.reportDialog.open({
      targetType: 'post',
      targetId: post.id,
      label: `"${post.title}"`,
    });
  }

  reportComment(comment: Comment) {
    this.reportDialog.open({
      targetType: 'comment',
      targetId: comment.id,
      label: `${comment.author.username}'s comment`,
    });
  }

  startCommentEdit(comment: Comment) {
    if (!this.isOwnComment(comment)) {
      return;
    }

    this.editingCommentId.set(comment.id);
    this.commentEditForm.setValue({ content: comment.content });
  }

  cancelCommentEdit() {
    this.editingCommentId.set(null);
    this.commentEditForm.reset();
  }

  saveCommentEdit(comment: Comment) {
    const post = this.post();

    if (!post || this.commentEditForm.invalid || this.savingCommentId()) {
      this.commentEditForm.markAllAsTouched();
      return;
    }

    this.savingCommentId.set(comment.id);
    this.commentService
      .updateComment(post.id, comment.id, this.commentEditForm.controls.content.value.trim())
      .subscribe({
        next: (updatedComment) => {
          this.comments.update((comments) =>
            comments.map((item) => (item.id === comment.id ? updatedComment : item)),
          );
          this.cancelCommentEdit();
          this.savingCommentId.set(null);
        },
        error: (err) => {
          console.error('Error updating comment:', err);
          this.errorMessage.set(err.error || 'Could not update this comment.');
          this.savingCommentId.set(null);
        },
      });
  }

  deleteComment(comment: Comment) {
    const post = this.post();

    if (!post || !this.isOwnComment(comment) || !confirm('Delete this comment?')) {
      return;
    }

    this.commentService.deleteComment(post.id, comment.id).subscribe({
      next: () => {
        this.comments.update((comments) => comments.filter((item) => item.id !== comment.id));
        this.post.set({
          ...post,
          totalComments: Math.max(0, post.totalComments - 1),
        });
      },
      error: (err) => {
        console.error('Error deleting comment:', err);
        this.errorMessage.set(err.error || 'Could not delete this comment.');
      },
    });
  }

  toggleCommentLike(comment: Comment) {
    const post = this.post();

    if (!post) {
      return;
    }

    this.commentService.toggleLike(post.id, comment.id).subscribe({
      next: (response) => {
        this.comments.update((comments) =>
          comments.map((item) =>
            item.id === comment.id
              ? {
                  ...item,
                  likedByCurrentUser: response.isLiked,
                  likeCount: response.totalLikes,
                }
              : item,
          ),
        );
      },
      error: (err) => console.error('Error toggling comment like:', err),
    });
  }

  isVideoUrl(url: string) {
    return /\.(mp4|webm|ogg|mov)(\?.*)?$/i.test(url);
  }

  mediaItems = computed<readonly MediaCarouselItem[]>(() =>
    (this.post()?.media ?? []).map((media) => ({
      url: media.url,
      kind: this.isVideoUrl(media.url) ? 'video' : 'image',
    })),
  );

  onMediaFilesSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const post = this.post();
    const files = input.files ? Array.from(input.files) : [];

    if (!post || !files.length || this.isUploadingMedia()) {
      input.value = '';
      return;
    }

    this.isUploadingMedia.set(true);

    forkJoin(files.map((file) => this.postService.addFileToPost(post.id, file))).subscribe({
      next: (newMedia) => {
        this.post.update((current) =>
          current ? { ...current, media: [...current.media, ...newMedia] } : current,
        );
        this.isUploadingMedia.set(false);
        this.toastService.success(
          newMedia.length > 1 ? 'Files added.' : 'File added.',
        );
      },
      error: (err) => {
        console.error('Error adding file to post:', err);
        this.isUploadingMedia.set(false);
        this.toastService.error(err.error || 'Could not upload this file.');
      },
    });

    input.value = '';
  }

  removeMedia(index: number) {
    const post = this.post();
    const media = post?.media[index];

    if (!post || !media || this.isUploadingMedia()) {
      return;
    }

    this.postService.deleteFileFromPost(post.id, media.id).subscribe({
      next: () => {
        this.post.update((current) =>
          current
            ? { ...current, media: current.media.filter((item) => item.id !== media.id) }
            : current,
        );
        this.toastService.success('File removed.');
      },
      error: (err) => {
        console.error('Error removing file from post:', err);
        this.toastService.error(err.error || 'Could not remove this file.');
      },
    });
  }

  private patchPostEditForm(post: Post) {
    this.postEditForm.setValue({
      title: post.title,
      content: post.content,
      tags: post.tag.join(', '),
    });
  }

  private loadComments(postId: number, cursor?: number | null) {
    this.commentsLoading.set(true);

    this.commentService.getComments(postId, cursor).subscribe({
      next: (response) => {
        this.comments.update((comments) =>
          cursor ? [...comments, ...response.data] : response.data,
        );
        this.commentsCursor.set(response.nextCursor);
        this.hasMoreComments.set(response.hasMore);
        this.commentsLoading.set(false);
      },
      error: (err) => {
        console.error('Error loading comments:', err);
        this.commentsLoading.set(false);
      },
    });
  }
}
