import { DatePipe, isPlatformBrowser, TitleCasePipe } from '@angular/common';
import { Component, inject, OnInit, PLATFORM_ID, signal } from '@angular/core';
import { AdminAction, AdminReport } from '../../../core/models';
import { AdminService } from '../../../core/services/admin.service';
import { AdminUiService } from '../shared/admin-ui.service';

const PAGE_SIZE = 20;

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [DatePipe, TitleCasePipe],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.scss',
})
export class ReportsComponent implements OnInit {
  private adminService = inject(AdminService);
  private ui = inject(AdminUiService);
  private platformId = inject(PLATFORM_ID);

  reports = signal<readonly AdminReport[]>([]);
  loading = signal(true);
  loadingMore = signal(false);
  page = signal(0);
  isLast = signal(true);

  ngOnInit(): void {
    // The JWT lives in localStorage, which doesn't exist during SSR — skip
    // the fetch there instead of firing an unauthenticated request.
    if (isPlatformBrowser(this.platformId)) {
      this.loadPage(0);
    }
  }

  loadMore(): void {
    if (this.isLast() || this.loadingMore()) {
      return;
    }
    this.loadPage(this.page() + 1);
  }

  primaryActionLabel(report: AdminReport): string {
    switch (report.reportType) {
      case 'USER':
        return 'Ban user';
      case 'COMMENT':
        return 'Remove comment';
      default:
        return 'Hide post';
    }
  }

  dismiss(report: AdminReport): void {
    this.ui
      .confirm({
        title: 'Dismiss this report?',
        description: 'This report will be marked as reviewed with no action taken.',
        confirmLabel: 'Dismiss report',
        danger: false,
      })
      .then((confirmed) => {
        if (confirmed) {
          this.resolve(report, 'DISMISS', 'Report dismissed.');
        }
      });
  }

  takeAction(report: AdminReport): void {
    const action = this.primaryAdminAction(report);
    const label = this.primaryActionLabel(report);

    const description =
      action === 'BAN_USER'
        ? `${report.reportedUsername} will no longer be able to sign in or post on 01.`
        : report.reportType === 'COMMENT'
          ? 'This comment will be removed and replaced with a moderation notice.'
          : 'This post will be hidden from the public.';

    this.ui
      .confirm({
        title: `${label}?`,
        description,
        confirmLabel: label,
      })
      .then((confirmed) => {
        if (confirmed) {
          this.resolve(report, action, `${label} — done.`);
        }
      });
  }

  private primaryAdminAction(report: AdminReport): AdminAction {
    return report.reportType === 'USER' ? 'BAN_USER' : 'HIDE_CONTENT';
  }

  private resolve(report: AdminReport, action: AdminAction, toastMessage: string): void {
    this.adminService.resolveReport(report.reportId, action).subscribe({
      next: () => {
        this.reports.update((items) => items.filter((r) => r.reportId !== report.reportId));
        this.ui.decrementPendingCount();
        this.ui.showToast(toastMessage);
      },
      error: () => this.ui.showToast('Something went wrong. Please try again.'),
    });
  }

  private loadPage(page: number): void {
    const isFirstPage = page === 0;
    isFirstPage ? this.loading.set(true) : this.loadingMore.set(true);

    this.adminService.getPendingReports(page, PAGE_SIZE).subscribe({
      next: (response) => {
        this.reports.update((items) =>
          isFirstPage ? response.content : [...items, ...response.content],
        );
        this.page.set(response.number);
        this.isLast.set(response.last);
        this.loading.set(false);
        this.loadingMore.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.loadingMore.set(false);
      },
    });
  }
}
