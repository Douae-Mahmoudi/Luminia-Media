 
import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { CollectionService } from '../../../core/services/collection.service';
import { MediaService } from '../../../core/services/media.service';
import { Collection } from '../../../core/models/collection.models';
import { MediaResponse, MediaType } from '../../../core/models/media.models';
 
interface CollectionMedia {
  id:           string;
  title:        string;
  author:       string;
  genre?:       string;
  imageUrl?:    string;
  type?:        MediaType;
  description?: string;
  releaseYear?: number;
  contentUrl?:  string;
  externalId?:  string;
  isExternal:   boolean;
}
 
@Component({
  selector: 'app-collection-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './collection-detail.component.html',
})
export class CollectionDetailComponent implements OnInit {
 
  collection   = signal<Collection | null>(null);
  mediaItems   = signal<CollectionMedia[]>([]);
  loading      = signal<boolean>(true);
  loadingMedia = signal<boolean>(false);
  removing     = signal<string | null>(null);
 
  constructor(
    private route:             ActivatedRoute,
    public  router:            Router,
    private collectionService: CollectionService,
    private mediaService:      MediaService,
  ) {}
 
  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) { this.router.navigate(['/collections']); return; }
    this.loadCollection(id);
  }
 
  private loadCollection(id: string): void {
    this.loading.set(true);
    this.collectionService.getById(id).subscribe({
      next: (col) => {
        if (!col?.id) { this.router.navigate(['/collections']); return; }
        this.collection.set(col);
        this.loading.set(false);
        this.loadMediaItems(col.mediaIds ?? []);
      },
      error: () => {
        this.loading.set(false);
        this.router.navigate(['/collections']);
      },
    });
  }
 
  private loadMediaItems(mediaIds: string[]): void {
    if (!mediaIds.length) { this.mediaItems.set([]); return; }
 
    this.loadingMedia.set(true);
    const items: CollectionMedia[] = [];
    let pending = mediaIds.length;
 
    const done = () => {
      pending--;
      if (pending === 0) {
        const ordered = mediaIds
          .map(id => items.find(it => it.id === id))
          .filter(Boolean) as CollectionMedia[];
        this.mediaItems.set(ordered);
        this.loadingMedia.set(false);
      }
    };
 
    for (const mediaId of mediaIds) {
      if (mediaId.startsWith('EXT_')) {
        const saved = this.getExternalMeta(mediaId);
 
        items.push({
          id:          mediaId,
          title:       saved?.title      ?? this.labelFromExtId(mediaId),
          author:      saved?.author     ?? '',
          genre:       saved?.genre,
          imageUrl:    saved?.imageUrl,
          type:        saved?.type       ?? this.parseTypeFromExtId(mediaId),
          description: saved?.description,
          releaseYear: saved?.releaseYear,
          externalId:  mediaId,
          isExternal:  true,
        });
        done();
      } else {
        const numId = Number(mediaId);
        if (!numId) {
          items.push({
            id: mediaId, title: `Média #${mediaId}`,
            author: '', isExternal: false,
          });
          done();
          continue;
        }
 
        this.mediaService.getById(numId).subscribe({
          next: (media: MediaResponse) => {
            items.push({
              id:          String(media.id),
              title:       media.title,
              author:      media.author,
              genre:       media.genre,
              imageUrl:    media.imageUrl,
              type:        media.type,
              description: media.description,
              releaseYear: media.releaseYear,
              contentUrl:  media.contentUrl,
              isExternal:  false,
            });
            done();
          },
          error: () => {
            items.push({
              id: mediaId, title: `Média #${mediaId}`,
              author: '', isExternal: false,
            });
            done();
          },
        });
      }
    }
  }
 
  private getExternalMeta(externalId: string): {
    title?: string; author?: string; genre?: string; imageUrl?: string;
    type?: MediaType; description?: string; releaseYear?: number;
  } | null {
    try {
      const raw = localStorage.getItem(`lumina_ext_meta_${externalId}`);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }
 
  private parseTypeFromExtId(extId: string): MediaType {
    const match = extId.match(/^EXT_(FILM|BOOK|GAME|PODCAST)_/);
    return (match?.[1] as MediaType) ?? 'FILM';
  }
 
  private labelFromExtId(extId: string): string {
    const parts = extId.split('_');
    if (parts.length >= 3) {
      return decodeURIComponent(parts.slice(2).join('_'))
        .replace(/-/g, ' ')
        .replace(/\b\w/g, c => c.toUpperCase());
    }
    return extId;
  }
 
  removeMedia(mediaId: string): void {
    const col = this.collection();
    if (!col) return;
    this.removing.set(mediaId);
 
    this.collectionService.removeMedia(col.id, mediaId).subscribe({
      next: () => {
        this.mediaItems.update(list => list.filter(m => m.id !== mediaId));
        this.collection.update(c => c ? {
          ...c,
          mediaIds:   (c.mediaIds ?? []).filter(id => id !== mediaId),
          mediaCount: Math.max(0, (c.mediaCount ?? 1) - 1),
        } : c);
        this.removing.set(null);
      },
      error: () => this.removing.set(null),
    });
  }
 
  navigateToMedia(item: CollectionMedia): void {
    if (item.isExternal) {
      this.router.navigate(['/media/external'], {
        state: {
          title:       item.title,
          author:      item.author,
          genre:       item.genre,
          coverUrl:    item.imageUrl,
          type:        item.type,
          description: item.description,
          releaseYear: item.releaseYear,
          externalId:  item.externalId,
        },
      });
    } else {
      this.router.navigate(['/media', item.id]);
    }
  }
 
  getTypeBadgeClass(type?: string): string {
    const map: Record<string, string> = {
      FILM:    'bg-blue-100 text-blue-700',
      BOOK:    'bg-amber-100 text-amber-700',
      GAME:    'bg-purple-100 text-purple-700',
      PODCAST: 'bg-green-100 text-green-700',
    };
    return map[type ?? ''] ?? 'bg-gray-100 text-gray-600';
  }
 
  getTypeLabel(type?: string): string {
    const map: Record<string, string> = {
      FILM: 'Film', BOOK: 'Livre', GAME: 'Jeu', PODCAST: 'Podcast',
    };
    return map[type ?? ''] ?? (type ?? '');
  }
 
  formatDate(d: string): string {
    return new Date(d).toLocaleDateString('fr-FR', {
      day: '2-digit', month: 'short', year: 'numeric',
    });
  }
}
