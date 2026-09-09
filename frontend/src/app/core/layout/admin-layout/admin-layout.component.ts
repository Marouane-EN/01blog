import { isPlatformBrowser } from '@angular/common';
import { Component, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthStore } from '../../store/auth.store';
import { AdminOverlaysComponent } from '../../../features/admin/shared/admin-overlays/admin-overlays.component';
import { AdminUiService } from '../../../features/admin/shared/admin-ui.service';
import { AvatarComponent } from '../../../shared/components/avatar/avatar.component';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RouterOutlet, AdminOverlaysComponent, AvatarComponent],
  templateUrl: './admin-layout.component.html',
  styleUrl: './admin-layout.component.scss',
})
export class AdminLayoutComponent implements OnInit {
  authStore = inject(AuthStore);
  ui = inject(AdminUiService);
  private platformId = inject(PLATFORM_ID);

  ngOnInit(): void {
    // The JWT lives in localStorage, which doesn't exist during SSR — skip
    // the fetch there instead of firing an unauthenticated request.
    if (isPlatformBrowser(this.platformId)) {
      this.ui.refreshPendingCount();
    }
  }
}
