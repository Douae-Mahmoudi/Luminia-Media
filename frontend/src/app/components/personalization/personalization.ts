import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule, Users, TrendingUp, Heart } from 'lucide-angular';
 
@Component({
  selector: 'app-personalization',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './personalization.html',
})
export class PersonalizationComponent {
 
  activeFeature = signal<number>(-1);
 
  readonly Users = Users;
  readonly TrendingUp = TrendingUp;
  readonly Heart = Heart;
 
  features = [
    {
      icon: Users,
      title: 'Insights Communautaires',
      desc: 'Découvrez ce que les personnes aux goûts similaires apprécient en ce moment.',
    },
    {
      icon: TrendingUp,
      title: 'Tendances du Moment',
      desc: 'Restez à jour avec ce qui est populaire dans vos catégories favorites.',
    },
    {
      icon: Heart,
      title: 'Vos Favoris',
      desc: 'Créez des collections et obtenez des recommandations basées sur ce que vous aimez.',
    },
  ];
}