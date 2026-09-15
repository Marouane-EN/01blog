import { inject, Injectable, signal } from '@angular/core';
import { AdminService } from '../../../core/services/admin.service';
import { ToastService } from '../../../core/services/toast.service';
import { extractErrorMessage } from '../../../core/utils/http-error.utils';

@Injectable({
  providedIn: 'root',
})
export class AdminUiService {
  private adminService = inject(AdminService);
  private toastService = inject(ToastService);

  readonly pendingReportsCount = signal(0);

  refreshPendingCount(): void {
    this.adminService.getPendingReports(0, 1).subscribe({
      next: (page) => this.pendingReportsCount.set(page.totalElements),
      error: (err) =>
        this.toastService.error(extractErrorMessage(err, 'Could not load pending reports count.')),
    });
  }

  decrementPendingCount(): void {
    this.pendingReportsCount.update((count) => Math.max(0, count - 1));
  }
}
