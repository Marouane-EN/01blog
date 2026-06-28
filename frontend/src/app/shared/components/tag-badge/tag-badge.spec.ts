import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TagBadge } from './tag-badge.component';

describe('TagBadge', () => {
  let component: TagBadge;
  let fixture: ComponentFixture<TagBadge>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TagBadge],
    }).compileComponents();

    fixture = TestBed.createComponent(TagBadge);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
