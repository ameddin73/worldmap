import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MapUiComponent } from './map-ui.component';

describe('MapUiComponent', () => {
  let component: MapUiComponent;
  let fixture: ComponentFixture<MapUiComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MapUiComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MapUiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
