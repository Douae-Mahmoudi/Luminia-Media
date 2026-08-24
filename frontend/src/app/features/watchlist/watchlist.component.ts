 
import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { InteractionService } from '../../core/services/interaction.service';
import { MediaService } from '../../core/services/media.service';
import { AuthService } from '../../core/services/auth';
import { MediaResponse } from '../../core/models/media.models';
 
@Component({
  selector: 'app-watchlist',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './watchlist.html',
})
export class WatchlistComponent implements OnInit {
 
  items     = signal<MediaResponse[]>([]);
  loading   = signal(true);
  username  = '';
  skeletons = Array(8);
 
  private router             = inject(Router);
  private authService        = inject(AuthService);
  private interactionService = inject(InteractionService);
  private mediaService       = inject(MediaService);
 
  ngOnInit(): void {
    this.username = this.authService.getUsername() ?? '';
    this.load();
  }
 
  private load(): void {
    this.loading.set(true);
 
    const externalResults: MediaResponse[] = [];
    const prefix = `lumina_ext_personal_${this.username}_`;
 
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (!key?.startsWith(prefix)) continue;
      try {
        const val = JSON.parse(localStorage.getItem(key)!);
        if (val?.inWatchlist === true && val?.title) {
          const externalId = decodeURIComponent(key.replace(prefix, ''));
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
            externalId,   
          } as MediaResponse);
        }
      } catch {}
    }
 
    try {
      const raw      = localStorage.getItem(`lumina_user_${encodeURIComponent(this.username)}`);
      const userData: Record<string, any> = raw ? JSON.parse(raw) : {};
      const ids = Object.entries(userData)
        .filter(([, v]) => v?.inWatchlist === true)
        .map(([k]) => Number(k))
        .filter(id => id > 0);
 
      if (!ids.length) {
        this.items.set(externalResults);
        this.loading.set(false);
        return;
      }
 
      let done = 0;
      const internalResults: MediaResponse[] = [];
 
      ids.forEach(id => {
        this.mediaService.getById(id).subscribe({
          next: m => {
            internalResults.push(m);
            if (++done === ids.length) {
              this.items.set([...internalResults, ...externalResults]);
              this.loading.set(false);
            }
          },
          error: () => {
            if (++done === ids.length) {
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
 
  remove(event: Event, media: MediaResponse): void {
    event.stopPropagation();
 
    if (media.id === 0 || media.externalId) {
      const key = media.externalId ?? media.title;
      this.interactionService.removeFromExternalWatchlist(key).subscribe();
    } else {
      this.interactionService.removeFromWatchlist(media.id).subscribe();
    }
 
    this.items.update(l => l.filter(i => !(i.id === media.id && i.title === media.title)));
  }
 
  navigate(media: MediaResponse): void {
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
          externalId:  media.externalId ?? media.title, 
        },
      });
    }
  }
 
  goDiscover(): void { this.router.navigate(['/dashboard']); }
 
  typeLabel(type: string): string {
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
 
  typeSoftBg(type: string): string {
    return ({
      FILM:    'rgba(59,91,219,.13)',
      BOOK:    'rgba(200,115,0,.13)',
      GAME:    'rgba(112,72,232,.13)',
      PODCAST: 'rgba(12,150,112,.13)',
    } as any)[type] ?? 'rgba(107,112,128,.1)';
  }
}
