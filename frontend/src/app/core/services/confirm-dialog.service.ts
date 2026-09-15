import { Injectable, signal } from '@angular/core';

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
export class ConfirmDialogService {
  readonly state = signal<ConfirmState | null>(null);

  confirm(options: ConfirmOptions): Promise<boolean> {
    return new Promise((resolve) => {
      this.state.set({ ...options, resolve });
    });
  }

  resolve(confirmed: boolean): void {
    this.state()?.resolve(confirmed);
    this.state.set(null);
  }
}
