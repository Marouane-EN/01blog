import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PostService } from '../../../core/services/post.service';

@Component({
  selector: 'app-create-post',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './create-post.component.html',
  styleUrls: ['./create-post.component.scss'],
})
export class CreatePostComponent {
  private fb = inject(FormBuilder);
  private postService = inject(PostService);
  private router = inject(Router);

  form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(128)]],
    content: ['', [Validators.required, Validators.maxLength(1000)]],
    tags: [''],
  });

  files = signal<readonly File[]>([]);
  isSubmitting = signal(false);
  errorMessage = signal('');

  onFilesSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    this.files.set(input.files ? Array.from(input.files) : []);
  }

  removeFile(fileToRemove: File) {
    this.files.update((files) => files.filter((file) => file !== fileToRemove));
  }

  submit() {
    if (this.form.invalid || this.isSubmitting()) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const tags = value.tags
      .split(',')
      .map((tag) => tag.trim())
      .filter(Boolean)
      .slice(0, 5);

    this.isSubmitting.set(true);
    this.errorMessage.set('');

    this.postService
      .createPost(
        {
          title: value.title.trim(),
          content: value.content.trim(),
          tags,
        },
        this.files(),
      )
      .subscribe({
        next: (post) => {
          this.isSubmitting.set(false);
          this.router.navigate(['/posts', post.slug]);
        },
        error: (err) => {
          console.error('Error creating post:', err);
          this.isSubmitting.set(false);
          this.errorMessage.set(err.error || 'Could not create the post. Please try again.');
        },
      });
  }
}
