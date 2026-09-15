import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { ReportDialogComponent } from '../../../shared/components/report-dialog/report-dialog.component';
import { ToastComponent } from '../../../shared/components/toast/toast.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, ReportDialogComponent, ToastComponent, ConfirmDialogComponent],
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.scss']
})
export class MainLayoutComponent {
}