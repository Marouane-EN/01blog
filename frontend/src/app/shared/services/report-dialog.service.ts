import { HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { ReportService } from '../../core/services/report.service';
import { ToastService } from '../../core/services/toast.service';

export type ReportTargetType = 'post' | 'comment' | 'user';

export interface ReportTarget {
  readonly targetType: ReportTargetType;
  readonly targetId: number;
  readonly label: string;
}

@Injectable({
  providedIn: 'root',
})
export class ReportDialogService {
  private reportService = inject(ReportService);
  private toastService = inject(ToastService);

  readonly target = signal<ReportTarget | null>(null);
  readonly submitting = signal(false);

  open(target: ReportTarget): void {
    // The trigger (a mat-menu item, a plain button…) otherwise keeps a
    // lingering focus ring once this dialog opens on top of it.
    if (typeof document !== 'undefined') {
      (document.activeElement as HTMLElement | null)?.blur();
    }
    this.target.set(target);
  }

  close(): void {
    if (this.submitting()) {
      return;
    }
    this.target.set(null);
  }

  submit(reason: string): void {
    const target = this.target();

    if (!target || this.submitting()) {
      return;
    }

    this.submitting.set(true);

    this.request(target, reason).subscribe({
      next: () => {
        this.submitting.set(false);
        this.target.set(null);
        this.toastService.success('Thanks — your report has been submitted.');
      },
      error: (err) => {
        this.submitting.set(false);
        this.toastService.error(this.extractErrorMessage(err));
      },
    });
  }

  private request(target: ReportTarget, reason: string) {
    switch (target.targetType) {
      case 'post':
        return this.reportService.reportPost(target.targetId, reason);
      case 'comment':
        return this.reportService.reportComment(target.targetId, reason);
      case 'user':
        return this.reportService.reportUser(target.targetId, reason);
    }
  }

  private extractErrorMessage(err: HttpErrorResponse): string {
    if (err.status === 0) {
      return "Couldn't reach the server. Check your connection and try again.";
    }
    if (typeof err.error === 'string' && err.error.trim()) {
      return err.error;
    }
    if (err.error?.message) {
      return err.error.message;
    }
    return 'Could not submit this report. Please try again.';
  }
}
