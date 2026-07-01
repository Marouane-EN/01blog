import { Component, Input, Output, EventEmitter, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { Post } from '../../../core/models/interfaces/post.model';
import { AuthStore } from '../../../core/store/auth.store';

@Component({
  selector: 'app-post-card',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule, MatButtonModule, MatMenuModule, DatePipe],
  templateUrl: './post-card.component.html',
  styleUrls: ['./post-card.component.scss'],
})
export class PostCardComponent {
  authStore = inject(AuthStore);

  @Input({ required: true }) post!: Post;

  @Output() likeClick = new EventEmitter<void>();
  @Output() deleteClick = new EventEmitter<void>();
  @Output() tagClick = new EventEmitter<string>();

  toggleLike(event: Event) {
    event.stopPropagation();
    this.likeClick.emit();
  }

  deletePost(event: Event) {
    event.stopPropagation();
    this.deleteClick.emit();
  }

  isOwnPost() {
    return this.authStore.user()?.id === this.post.author.id;
  }

  calculateReadingTime(content: string): number {
    if (!content) return 1;
    const wordsPerMinute = 200;
    const words = content.trim().split(/\s+/).length;
    return Math.max(1, Math.ceil(words / wordsPerMinute));
  }
}
