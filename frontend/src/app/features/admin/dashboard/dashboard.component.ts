import { DatePipe, isPlatformBrowser, TitleCasePipe } from '@angular/common';
import { Component, inject, OnInit, PLATFORM_ID, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AdminReport } from '../../../core/models';
import { AdminService } from '../../../core/services/admin.service';
import { AdminUiService } from '../shared/admin-ui.service';
import { ToastService } from '../../../core/services/toast.service';
import { extractErrorMessage } from '../../../core/utils/http-error.utils';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [DatePipe, TitleCasePipe, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  private adminService = inject(AdminService);
  private platformId = inject(PLATFORM_ID);
  private toastService = inject(ToastService);
  ui = inject(AdminUiService);

  totalUsers = signal<number | null>(null);
  totalPosts = signal<number | null>(null);
  attentionReports = signal<readonly AdminReport[]>([]);
  loading = signal(true);

  ngOnInit(): void {
    // The JWT lives in localStorage, which doesn't exist during SSR — skip
    // the fetch there instead of firing an unauthenticated request.
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    this.adminService.getUsers(0, 1).subscribe({
      next: (page) => this.totalUsers.set(page.totalElements),
      error: (err) => this.toastService.error(extractErrorMessage(err, 'Could not load total users.')),
    });

    this.adminService.getPosts(0, 1).subscribe({
      next: (page) => this.totalPosts.set(page.totalElements),
      error: (err) => this.toastService.error(extractErrorMessage(err, 'Could not load total posts.')),
    });

    this.adminService.getPendingReports(0, 5).subscribe({
      next: (page) => {
        this.attentionReports.set(page.content);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.toastService.error(extractErrorMessage(err, 'Could not load the dashboard.'));
      },
    });
  }
}
