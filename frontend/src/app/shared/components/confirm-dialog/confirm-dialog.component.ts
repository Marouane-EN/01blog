import { Component, HostListener, inject } from '@angular/core';
import { ConfirmDialogService } from '../../../core/services/confirm-dialog.service';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [],
  templateUrl: './confirm-dialog.component.html',
  styleUrl: './confirm-dialog.component.scss',
})
export class ConfirmDialogComponent {
  private confirmDialog = inject(ConfirmDialogService);

  state = this.confirmDialog.state;

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.state()) {
      this.confirmDialog.resolve(false);
    }
  }

  onBackdropClick(event: MouseEvent): void {
    if (event.target === event.currentTarget) {
      this.confirmDialog.resolve(false);
    }
  }

  resolve(confirmed: boolean): void {
    this.confirmDialog.resolve(confirmed);
  }
}
