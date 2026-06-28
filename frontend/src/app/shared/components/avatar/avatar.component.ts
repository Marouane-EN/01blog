import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';

export type AvatarSize = 'xs' | 'sm' | 'md' | 'lg';

@Component({
  selector: 'app-avatar',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './avatar.component.html',
  styleUrl: './avatar.component.scss',
})
export class AvatarComponent {
  @Input({ required: true }) displayName!: string;
  @Input() src: string | null = null;
  @Input() size: AvatarSize = 'md';

  get initials(): string {
    return this.displayName
      .split(' ')
      .slice(0, 2)
      .map((word) => word.charAt(0))
      .join('');
  }
}
