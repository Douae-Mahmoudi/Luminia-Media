import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DashboardUserComponent } from './dashboard-user';

describe('DashboardUserComponent', () => {
  let component: DashboardUserComponent;
  let fixture: ComponentFixture<DashboardUserComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardUserComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DashboardUserComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});