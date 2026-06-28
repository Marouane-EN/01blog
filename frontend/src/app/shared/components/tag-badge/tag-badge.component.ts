import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';
import { Tag } from '../../../core/models/interfaces/post.model';

@Component({
  selector: 'app-tag-badge',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './tag-badge.component.html',
  styleUrl: './tag-badge.component.scss',
})
export class TagBadgeComponent {
  @Input({ required: true }) tag!: Tag;
  @Output() tagClick = new EventEmitter<Tag>();
}
