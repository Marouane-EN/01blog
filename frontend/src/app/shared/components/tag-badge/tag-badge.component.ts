import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-tag-badge',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './tag-badge.component.html',
  styleUrl: './tag-badge.component.scss',
})
export class TagBadgeComponent {
  @Input({ required: true }) tag!: string;
  @Output() tagClick = new EventEmitter<string>();
}
