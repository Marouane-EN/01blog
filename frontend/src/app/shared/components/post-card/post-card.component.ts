import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { Post } from '../../../core/models/interfaces/post.model';

@Component({
  selector: 'app-post-card',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule, MatButtonModule, DatePipe],
  templateUrl: './post-card.component.html',
  styleUrls: ['./post-card.component.scss'],
})
export class PostCardComponent {
  @Input({ required: true }) post!: Post;

  @Output() likeClick = new EventEmitter<void>();
  @Output() tagClick = new EventEmitter<string>();

  toggleLike(event: Event) {
    event.stopPropagation();
    this.likeClick.emit();
  }

  calculateReadingTime(content: string): number {
    if (!content) return 1;
    const wordsPerMinute = 200;
    const words = content.trim().split(/\s+/).length;
    return Math.max(1, Math.ceil(words / wordsPerMinute));
  }
}
