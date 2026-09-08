import { Component, OnInit, effect, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { AuthStore } from '../../../core/store/auth.store';
import { Notification as AppNotification } from '../../../core/models';
import { NotificationService } from '../../../core/services/notification.service';
import { MatSpinner } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    RouterModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDividerModule,
    MatBadgeModule,
    MatSpinner
  ],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss'],
})
export class NavbarComponent implements OnInit {
  // Inject your awesome signal store!
  authStore = inject(AuthStore);
  private router = inject(Router);
  private notificationService = inject(NotificationService);

  searchValue = '';
  notifications = signal<readonly AppNotification[]>([]);
  unreadCount = signal(0);
  notificationsLoading = signal(false);

  constructor() {
    effect(() => {
      if (this.authStore.isAuthenticated()) {
        this.loadUnreadCount();
      }
    });
  }

  ngOnInit() {
    if (this.authStore.isAuthenticated()) {
      this.loadUnreadCount();
    }
  }

  submitSearch() {
    const query = this.searchValue.trim();

    this.router.navigate(['/'], {
      queryParams: query ? { q: query } : {},
    });
  }

  loadNotifications() {
    if (this.notificationsLoading()) {
      return;
    }

    this.notificationsLoading.set(true);
    this.notificationService.getNotifications().subscribe({
      next: (response) => {
        this.notifications.set(response.data);
        this.notificationsLoading.set(false);
      },
      error: (err) => {
        console.error('Error loading notifications:', err);
        this.notificationsLoading.set(false);
      },
    });
  }

  markNotification(notification: AppNotification) {
    if (!notification.isRead) {
      this.notificationService.toggleRead(notification.id).subscribe({
        next: () => {
          this.unreadCount.update((count) => Math.max(0, count - 1));
          this.notifications.update((items) =>
            items.map((item) => (item.id === notification.id ? { ...item, isRead: true } : item)),
          );
        },
        error: (err) => console.error('Error marking notification:', err),
      });
    }
  }

  private loadUnreadCount() {
    this.notificationService.getUnreadCount().subscribe({
      next: (count) => this.unreadCount.set(count),
      error: (err) => console.error('Error loading unread notifications:', err),
    });
  }
}
