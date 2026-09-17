import { Component, computed, inject, OnDestroy, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { PostService } from '../../../core/services/post.service';
import { ToastService } from '../../../core/services/toast.service';
import { extractErrorMessage } from '../../../core/utils/http-error.utils';
import {
  MediaCarouselComponent,
  MediaCarouselItem,
} from '../../../shared/components/media-carousel/media-carousel.component';

interface FilePreview {
  readonly file: File;
  readonly url: string;
  readonly kind: 'image' | 'video';
}

@Component({
  selector: 'app-create-post',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, MediaCarouselComponent],
  templateUrl: './create-post.component.html',
  styleUrls: ['./create-post.component.scss'],
})
export class CreatePostComponent implements OnDestroy {
  private fb = inject(FormBuilder);
  private postService = inject(PostService);
  private router = inject(Router);
  private toastService = inject(ToastService);

  form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(128)]],
    content: ['', [Validators.required, Validators.maxLength(1000)]],
    tags: [''],
  });

  previews = signal<readonly FilePreview[]>([]);
  activeIndex = signal(0);
  isSubmitting = signal(false);
  errorMessage = signal('');

  mediaItems = computed<readonly MediaCarouselItem[]>(() =>
    this.previews().map((preview) => ({
      url: preview.url,
      kind: preview.kind,
      label: preview.file.name,
    })),
  );

  onFilesSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const newFiles = input.files ? Array.from(input.files) : [];

    if (newFiles.length) {
      const newPreviews: FilePreview[] = newFiles.map((file) => ({
        file,
        url: URL.createObjectURL(file),
        kind: file.type.startsWith('video/') ? 'video' : 'image',
      }));

      const insertAt = this.previews().length;
      this.previews.update((previews) => [...previews, ...newPreviews]);
      this.activeIndex.set(insertAt);
    }

    // Allow re-selecting the same file again later.
    input.value = '';
  }

  removePreviewAt(index: number) {
    const preview = this.previews()[index];

    if (!preview) {
      return;
    }

    URL.revokeObjectURL(preview.url);
    this.previews.update((previews) => previews.filter((_, i) => i !== index));
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
        this.previews().map((preview) => preview.file),
      )
      .subscribe({
        next: (post) => {
          this.isSubmitting.set(false);
          this.toastService.success('Post published.');
          this.router.navigate(['/posts', post.slug]);
        },
        error: (err) => {
          this.isSubmitting.set(false);
          const message = extractErrorMessage(err, 'Could not create the post. Please try again.');
          this.errorMessage.set(message);
          this.toastService.error(message);
        },
      });
  }

  ngOnDestroy() {
    this.previews().forEach((preview) => URL.revokeObjectURL(preview.url));
  }
}
