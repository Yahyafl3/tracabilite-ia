import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LayoutService } from '../layout.service';

@Component({
  selector: 'app-configurator',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './app-configurator.component.html',
  styleUrl: './app-configurator.component.scss',
})
export class AppConfiguratorComponent {
  readonly layoutService = inject(LayoutService);

  setMenuMode(mode: 'static' | 'overlay'): void {
    this.layoutService.layoutConfig.update((c) => ({ ...c, menuMode: mode }));
  }
}
