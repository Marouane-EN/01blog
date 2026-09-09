import { inject, Injectable, signal } from '@angular/core';
import { AdminService } from '../../../core/services/admin.service';

export interface ConfirmOptions {
  readonly title: string;
  readonly description: string;
  readonly confirmLabel: string;
  readonly danger?: boolean;
}

interface ConfirmState extends ConfirmOptions {
  readonly resolve: (confirmed: boolean) => void;
}

@Injectable({
  providedIn: 'root',
})
export class AdminUiService {
  private adminService = inject(AdminService);

  private toastTimer?: ReturnType<typeof setTimeout>;

  readonly toastMessage = signal<string | null>(null);
  readonly confirmState = signal<ConfirmState | null>(null);
  readonly pendingReportsCount = signal(0);

  showToast(message: string): void {
    this.toastMessage.set(message);
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toastMessage.set(null), 2800);
  }

  confirm(options: ConfirmOptions): Promise<boolean> {
    return new Promise((resolve) => {
      this.confirmState.set({ ...options, resolve });
    });
  }

  resolveConfirm(confirmed: boolean): void {
    this.confirmState()?.resolve(confirmed);
    this.confirmState.set(null);
  }

  refreshPendingCount(): void {
    this.adminService.getPendingReports(0, 1).subscribe({
      next: (page) => this.pendingReportsCount.set(page.totalElements),
      error: () => {},
    });
  }

  decrementPendingCount(): void {
    this.pendingReportsCount.update((count) => Math.max(0, count - 1));
  }
}
