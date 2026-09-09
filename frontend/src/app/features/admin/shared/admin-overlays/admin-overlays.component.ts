import { Component, HostListener, inject } from '@angular/core';
import { AdminUiService } from '../admin-ui.service';

@Component({
  selector: 'app-admin-overlays',
  standalone: true,
  imports: [],
  templateUrl: './admin-overlays.component.html',
  styleUrl: './admin-overlays.component.scss',
})
export class AdminOverlaysComponent {
  ui = inject(AdminUiService);

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.ui.confirmState()) {
      this.ui.resolveConfirm(false);
    }
  }

  onBackdropClick(event: MouseEvent): void {
    if (event.target === event.currentTarget) {
      this.ui.resolveConfirm(false);
    }
  }
}
