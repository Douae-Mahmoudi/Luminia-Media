
import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { InteractionService } from '../../core/services/interaction.service';
import { MediaService } from '../../core/services/media.service';
import { AuthService } from '../../core/services/auth';
import { MediaResponse } from '../../core/models/media.models';

@Component({
  selector: 'app-favorites',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './favorites.component.html',
})
export class FavoritesComponent implements OnInit {

  items        = signal<MediaResponse[]>([]);
  loading      = signal(true);
  activeFilter = signal<string>('ALL');
  username     = '';

  readonly filters = [
    { label: 'Tous',     value: 'ALL'     },
    { label: 'Films',    value: 'FILM'    },
    { label: 'Livres',   value: 'BOOK'    },
    { label: 'Jeux',     value: 'GAME'    },
    { label: 'Podcasts', value: 'PODCAST' },
  ];

  router                     = inject(Router);
  private authService        = inject(AuthService);
  private interactionService = inject(InteractionService);
  private mediaService       = inject(MediaService);

  ngOnInit(): void {
    this.username = this.authService.getUsername() ?? '';
    this.loadFavorites();
  }

  private loadFavorites(): void {
    this.loading.set(true);

    const externalResults: (MediaResponse & { externalKey: string })[] = [];
    const prefix = `lumina_ext_personal_${this.username}_`;

    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (!key?.startsWith(prefix)) continue;
      try {
        const val = JSON.parse(localStorage.getItem(key)!);
        if (val?.isFavorited === true && val?.title) {
          const externalKey = key.substring(prefix.length);
          externalResults.push({
            id:          0,
            title:       val.title       ?? '',
            author:      val.author      ?? '',
            genre:       val.genre       ?? '',
            imageUrl:    val.imageUrl    ?? '',
            type:        val.type        ?? 'FILM',
            releaseYear: val.releaseYear ?? undefined,
            status:      val.status      ?? 'AVAILABLE',
            description: val.description ?? '',
            externalKey: externalKey, 
          } as any);
        }
      } catch {}
    }

    try {
      const raw = localStorage.getItem(`lumina_user_${encodeURIComponent(this.username)}`);
      const userData: Record<string, any> = raw ? JSON.parse(raw) : {};

      const mediaIds = Object.entries(userData)
        .filter(([, v]) => v?.favorited === true)
        .map(([k]) => Number(k))
        .filter(id => !isNaN(id) && id > 0);

      if (mediaIds.length === 0) {
        this.items.set(externalResults);
        this.loading.set(false);
        return;
      }

      let loaded = 0;
      const internalResults: MediaResponse[] = [];

      mediaIds.forEach(id => {
        this.mediaService.getById(id).subscribe({
          next: (media) => {
            internalResults.push(media);
            loaded++;
            if (loaded === mediaIds.length) {
              this.items.set([...internalResults, ...externalResults]);
              this.loading.set(false);
            }
          },
          error: () => {
            loaded++;
            if (loaded === mediaIds.length) {
              this.items.set([...internalResults, ...externalResults]);
              this.loading.set(false);
            }
          },
        });
      });

    } catch {
      this.items.set(externalResults);
      this.loading.set(false);
    }
  }

  filteredItems(): MediaResponse[] {
    const f = this.activeFilter();
    return f === 'ALL' ? this.items() : this.items().filter(i => i.type === f);
  }

  countByType(type: string): number {
    return this.items().filter(i => i.type === type).length;
  }

  removeItem(event: Event, media: any): void {
    event.stopPropagation();

    if (media.id === 0) {
      const externalKey = media.externalKey;
      if (externalKey) {
        this.interactionService.removeFromExternalFavorites(externalKey).subscribe();
      }
    } else {
      this.interactionService.removeFromFavorites(media.id).subscribe();
    }

    this.items.update(list => list.filter(i => !(i.id === media.id && i.title === media.title)));
  }

  navigate(media: any): void {
    if (media.id && media.id > 0) {
      this.router.navigate(['/media', media.id]);
    } else {
      this.router.navigate(['/media/external'], {
        state: {
          title:       media.title,
          author:      media.author,
          genre:       media.genre,
          imageUrl:    media.imageUrl,
          type:        media.type,
          description: media.description,
          releaseYear: media.releaseYear,
          externalId:  media.externalKey,  
        },
      });
    }
  }

  getTypeLabel(type: string): string {
    return ({ FILM: 'Film', BOOK: 'Livre', GAME: 'Jeu', PODCAST: 'Podcast' } as any)[type] ?? type;
  }

  typeAccent(type: string): string {
    return ({
      FILM:    '#3B5BDB',
      BOOK:    '#C87300',
      GAME:    '#7048E8',
      PODCAST: '#0C9670',
    } as any)[type] ?? '#6B7080';
  }
}