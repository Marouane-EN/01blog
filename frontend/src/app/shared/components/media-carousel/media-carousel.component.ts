import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
} from '@angular/core';

export interface MediaCarouselItem {
  readonly url: string;
  readonly kind: 'image' | 'video';
  readonly label?: string;
}

@Component({
  selector: 'app-media-carousel',
  standalone: true,
  imports: [],
  templateUrl: './media-carousel.component.html',
  styleUrl: './media-carousel.component.scss',
})
export class MediaCarouselComponent implements OnChanges {
  @Input({ required: true }) items: readonly MediaCarouselItem[] = [];
  @Input() removable = false;
  @Input() activeIndex = 0;

  @Output() activeIndexChange = new EventEmitter<number>();
  @Output() remove = new EventEmitter<number>();

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes['items']) {
      return;
    }

    if (this.items.length === 0 && this.activeIndex !== 0) {
      this.setActiveIndex(0);
    } else if (this.activeIndex > this.items.length - 1) {
      this.setActiveIndex(Math.max(0, this.items.length - 1));
    }
  }

  get activeItem(): MediaCarouselItem | null {
    return this.items[this.activeIndex] ?? null;
  }

  previous(): void {
    if (this.items.length > 1) {
      this.setActiveIndex((this.activeIndex - 1 + this.items.length) % this.items.length);
    }
  }

  next(): void {
    if (this.items.length > 1) {
      this.setActiveIndex((this.activeIndex + 1) % this.items.length);
    }
  }

  goTo(index: number): void {
    this.setActiveIndex(index);
  }

  removeActive(): void {
    this.remove.emit(this.activeIndex);
  }

  private setActiveIndex(index: number): void {
    this.activeIndex = index;
    this.activeIndexChange.emit(index);
  }
}
