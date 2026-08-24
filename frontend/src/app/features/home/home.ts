 
import { Component } from '@angular/core';
import { NavbarComponent } from '../../components/navbar/navbar';
import { HeroComponent } from '../../components/hero/hero';
import { CategoriesComponent } from '../../components/categories/categories';
import { PersonalizationComponent } from '../../components/personalization/personalization';
import { FooterComponent } from '../../components/footer/footer';
 
@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    NavbarComponent,
    HeroComponent,
    CategoriesComponent,
    PersonalizationComponent,
    FooterComponent,
  ],
  template: `
    <app-navbar></app-navbar>
    <app-hero></app-hero>
    <app-categories></app-categories>
    <app-personalization></app-personalization>
    <app-footer></app-footer>
  `,
})
export class HomeComponent {}
 


