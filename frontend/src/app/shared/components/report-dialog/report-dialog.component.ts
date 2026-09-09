import { Component, computed, effect, HostListener, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ReportDialogService, ReportTargetType } from '../../services/report-dialog.service';

const MIN_REASON_LENGTH = 10;
const MAX_REASON_LENGTH = 500;

const SUGGESTIONS_BY_TYPE: Record<ReportTargetType, readonly string[]> = {
  post: [
    'This is spam or unwanted advertising.',
    'This contains inappropriate or explicit content.',
    'This is harassment or bullying.',
    'This is misinformation or false information.',
  ],
  comment: [
    'This is spam or unwanted advertising.',
    'This is harassment or bullying.',
    'This contains inappropriate or explicit content.',
    'This is off-topic or irrelevant.',
  ],
  user: [
    'This account is harassing or bullying others.',
    'This account is impersonating someone else.',
    'This looks like a spam or fake account.',
    'This profile contains inappropriate content.',
  ],
};

@Component({
  selector: 'app-report-dialog',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './report-dialog.component.html',
  styleUrl: './report-dialog.component.scss',
})
export class ReportDialogComponent {
  dialog = inject(ReportDialogService);

  readonly minLength = MIN_REASON_LENGTH;
  readonly maxLength = MAX_REASON_LENGTH;
  reason = signal('');

  readonly suggestions = computed(() => {
    const target = this.dialog.target();
    return target ? SUGGESTIONS_BY_TYPE[target.targetType] : [];
  });

  constructor() {
    // Reset the draft whenever a new target opens (or the dialog closes).
    effect(() => {
      this.dialog.target();
      this.reason.set('');
    });
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.dialog.close();
  }

  onBackdropClick(event: MouseEvent): void {
    if (event.target === event.currentTarget) {
      this.dialog.close();
    }
  }

  selectSuggestion(suggestion: string): void {
    this.reason.set(suggestion);
  }

  submit(): void {
    const reason = this.reason().trim();

    if (reason.length < this.minLength) {
      return;
    }

    this.dialog.submit(reason);
  }
}
