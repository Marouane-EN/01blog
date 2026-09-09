import { Injectable, signal } from '@angular/core';

export type ToastVariant = 'success' | 'error' | 'info';

export interface Toast {
  readonly message: string;
  readonly variant: ToastVariant;
}

@Injectable({
  providedIn: 'root',
})
export class ToastService {
  private timer?: ReturnType<typeof setTimeout>;

  readonly toast = signal<Toast | null>(null);

  success(message: string): void {
    this.show(message, 'success');
  }

  error(message: string): void {
    this.show(message, 'error');
  }

  info(message: string): void {
    this.show(message, 'info');
  }

  dismiss(): void {
    clearTimeout(this.timer);
    this.toast.set(null);
  }

  private show(message: string, variant: ToastVariant): void {
    this.toast.set({ message, variant });
    clearTimeout(this.timer);
    this.timer = setTimeout(() => this.toast.set(null), variant === 'error' ? 4500 : 3000);
  }
}
