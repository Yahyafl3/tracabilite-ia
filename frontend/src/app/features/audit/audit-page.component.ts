import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { IconComponent } from '../../shared/icon.component';

@Component({
  selector: 'app-audit-page',
  standalone: true,
  imports: [CommonModule, RouterModule, IconComponent],
  templateUrl: './audit-page.component.html',
  styleUrl: './audit-page.component.scss',
})
export class AuditPageComponent {}
