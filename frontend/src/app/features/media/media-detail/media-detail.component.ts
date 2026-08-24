
import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MediaService } from '../../../core/services/media.service';
import { InteractionService } from '../../../core/services/interaction.service';
import { AuthService } from '../../../core/services/auth';
import { RecommendationService } from '../../../core/services/recommendation.service';
import { CollectionService } from '../../../core/services/collection.service';
import { MediaResponse, MediaType } from '../../../core/models/media.models';
import { Collection } from '../../../core/models/collection.models';
import { Comment } from '../../../core/models/interaction.models';

@Component({
  selector: 'app-media-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './media-detail.html',
})
export class MediaDetailComponent implements OnInit {

  media      = signal<MediaResponse | null>(null);
  related    = signal<MediaResponse[]>([]);
  loading    = signal<boolean>(true);
  isExternal = false;

  private externalKey = '';
  currentUsername     = '';

  isLiked    = signal(false);
  likesCount = signal(0);

  isFavorited   = signal(false);
  isInWatchlist = signal(false);
  userRating    = signal(0);
  hoveredRating = signal(0);

  likeLoading      = signal(false);
  favoriteLoading  = signal(false);
  watchlistLoading = signal(false);

  readonly stars = [1, 2, 3, 4, 5];

  comments        = signal<Comment[]>([]);
  newComment      = '';
  commentLoading  = signal(false);
  commentsLoading = signal(false);

  showCollectionModal = signal(false);
  collections         = signal<Collection[]>([]);
  collectionsLoading  = signal(false);
  addingToCollection  = signal<string | null>(null);
  addedToCollections  = signal<Set<string>>(new Set());

  public router = inject(Router);
  private route                 = inject(ActivatedRoute);
  private mediaService          = inject(MediaService);
  private interactionService    = inject(InteractionService);
  private authService           = inject(AuthService);
  private recommendationService = inject(RecommendationService);
  private collectionService     = inject(CollectionService);


  ngOnInit(): void {
    this.currentUsername = this.authService.getUsername() ?? 'utilisateur';

    const id = this.route.snapshot.paramMap.get('id');

    if (!id || id === 'external') {
      const state = history.state as {
        title?:       string;
        author?:      string;
        genre?:       string;
        imageUrl?:    string;
        coverUrl?:    string;
        readUrl?:     string;
        type?:        MediaType;
        source?:      string;
        description?: string;
        releaseYear?: number;
        externalId?:  string;
      };

      if (!state?.title) {
        this.router.navigate(['/dashboard']);
        return;
      }

      this.isExternal  = true;
      this.externalKey = state.externalId ?? state.title;

      const resolvedType: MediaType =
        state.type ??
        (state.externalId?.match(/^EXT_(FILM|BOOK|GAME|PODCAST)_/)?.[1] as MediaType) ??
        'FILM';

      this.media.set({
        id:          0,
        title:       state.title,
        author:      state.author      ?? '',
        genre:       state.genre       ?? '',
        imageUrl:    state.coverUrl    ?? state.imageUrl ?? '',
        contentUrl:  state.readUrl     ?? '',
        type:        resolvedType,
        description: state.description ?? '',
        status:      'AVAILABLE',
        releaseYear: state.releaseYear,
        externalId:  state.externalId,
      } as MediaResponse);

      this.loading.set(false);
      this.loadExternalData();
      return;
    }

    const numId = Number(id);
    if (!numId) {
      this.router.navigate(['/dashboard']);
      return;
    }

    this.mediaService.getById(numId).subscribe({
      next: (media) => {
        this.media.set(media);
        this.loading.set(false);
        this.loadRelated(media.type, numId);
        this.loadInternalData(numId);
      },
      error: () => {
        this.loading.set(false);
        this.router.navigate(['/dashboard']);
      },
    });
  }


  openContent(): void {
    const url = this.media()?.contentUrl;
    if (url) window.open(url, '_blank');
  }


  openCollectionModal(): void {
    this.showCollectionModal.set(true);
    this.collectionsLoading.set(true);
    const userId = this.authService.getUsername() ?? '';
    this.collectionService.getByUserId(userId).subscribe({
      next: (cols) => { this.collections.set(cols); this.collectionsLoading.set(false); },
      error: ()    => this.collectionsLoading.set(false),
    });
  }

  closeCollectionModal(): void {
    this.showCollectionModal.set(false);
  }

  addToCollection(collectionId: string): void {
    const m = this.media();
    if (!m) return;

    const mediaId = m.externalId ?? String(m.id);
    this.addingToCollection.set(collectionId);

    if (m.externalId) {
      const meta = {
        title: m.title, author: m.author, genre: m.genre,
        imageUrl: m.imageUrl, type: m.type, description: m.description,
        releaseYear: m.releaseYear, externalId: m.externalId,
      };
      try {
        localStorage.setItem(`lumina_ext_meta_${m.externalId}`, JSON.stringify(meta));
      } catch (e) {
        console.warn('[Collection] localStorage plein ou indisponible', e);
      }
    }

    this.collectionService.addMedia(collectionId, mediaId).subscribe({
      next: () => {
        this.addedToCollections.update(s => new Set([...s, collectionId]));
        this.addingToCollection.set(null);
      },
      error: () => this.addingToCollection.set(null),
    });
  }

  isAdded(collectionId: string): boolean {
    return this.addedToCollections().has(collectionId);
  }

 

  private loadInternalData(mediaId: number): void {
    const state = this.interactionService.getMediaState(mediaId);
    this.isLiked.set(state.liked);
    this.likesCount.set(state.likesCount);
    this.isFavorited.set(state.favorited);
    this.isInWatchlist.set(state.inWatchlist);
    this.userRating.set(state.userRating);

    this.interactionService.getLikesCountFromBackend(mediaId).subscribe(count => this.likesCount.set(count));
    this.interactionService.isLikedFromBackend(mediaId).subscribe(liked => this.isLiked.set(liked));
    this.interactionService.isFavoritedFromBackend(mediaId).subscribe(favorited => this.isFavorited.set(favorited));
    this.interactionService.isInWatchlistFromBackend(mediaId).subscribe(inWatchlist => this.isInWatchlist.set(inWatchlist));

    this.loadInternalComments(mediaId);
  }

  private loadInternalComments(mediaId: number): void {
    this.commentsLoading.set(true);
    this.interactionService.loadComments(mediaId).subscribe({
      next:  (comments) => { this.comments.set(comments); this.commentsLoading.set(false); },
      error: ()         => { this.comments.set([]);        this.commentsLoading.set(false); },
    });
  }



  private loadExternalData(): void {
    this.interactionService.getExternalLikeStateFromBackend(this.externalKey).subscribe({
      next: (state) => { this.isLiked.set(state.liked); this.likesCount.set(state.likesCount); },
      error: () => {
        const local = this.interactionService.getExternalLikeState(this.externalKey);
        this.isLiked.set(local.liked);
        this.likesCount.set(local.likesCount);
      },
    });

    this.interactionService.isExternalFavoritedFromBackend(this.externalKey).subscribe({
      next:  (favorited)   => this.isFavorited.set(favorited),
      error: ()            => this.isFavorited.set(false),
    });

    this.interactionService.isInExternalWatchlistFromBackend(this.externalKey).subscribe({
      next:  (inWatchlist) => this.isInWatchlist.set(inWatchlist),
      error: ()            => this.isInWatchlist.set(false),
    });

    const savedRating = localStorage.getItem(
      `lumina_ext_rating_${this.currentUsername}_${this.externalKey}`
    );
    if (savedRating) this.userRating.set(parseInt(savedRating, 10));

    this.commentsLoading.set(true);
    this.interactionService.loadExternalComments(this.externalKey).subscribe({
      next:  (comments) => { this.comments.set(comments); this.commentsLoading.set(false); },
      error: ()         => { this.comments.set([]);        this.commentsLoading.set(false); },
    });
  }

  private saveExternalRating(): void {
    localStorage.setItem(
      `lumina_ext_rating_${this.currentUsername}_${this.externalKey}`,
      String(this.userRating())
    );
  }



  loadRelated(type: string, currentId: number): void {
    this.mediaService.searchByType(type as MediaType).subscribe({
      next: (items) => this.related.set(items.filter(i => i.id !== currentId).slice(0, 4)),
      error: () => {},
    });
  }


  toggleLike(): void {
    if (this.likeLoading() || !this.media()) return;

    if (this.isExternal) {
      this.likeLoading.set(true);
      const action$ = this.isLiked()
        ? this.interactionService.unlikeExternal(this.externalKey)
        : this.interactionService.likeExternal(this.externalKey, this.media()!.title);

      action$.subscribe({
        next: (state) => {
          this.isLiked.set(state.liked);
          this.likesCount.set(state.likesCount);
          this.likeLoading.set(false);
          if (state.liked) {
            this.recommendationService.likeExternalMedia(
              this.externalKey, 'LIKE',
              this.media()!.title,
              this.media()!.type ?? '',
            ).subscribe();
          } else {
            this.recommendationService.unlikeExternalMedia(this.externalKey, 'LIKE').subscribe();
          }
        },
        error: () => this.likeLoading.set(false),
      });
      return;
    }

    this.likeLoading.set(true);
    const mediaId = this.media()!.id;
    const ownerId = String(this.media()!.ownerId ?? '0');

    const action$ = this.isLiked()
      ? this.interactionService.unlikeMedia(mediaId)
      : this.interactionService.likeMedia(mediaId, this.media()!.title, ownerId);

    action$.subscribe({
      next: () => {
        this.isLiked.update(v => !v);
        this.interactionService.getLikesCountFromBackend(mediaId).subscribe(count => {
          this.likesCount.set(count);
          this.likeLoading.set(false);
        });
      },
      error: () => this.likeLoading.set(false),
    });
  }


  toggleFavorite(): void {
    if (this.favoriteLoading() || !this.media()) return;

    if (this.isExternal) {
      this.favoriteLoading.set(true);
      const m = this.media()!;
      const wasFavorited = this.isFavorited(); 
  
      const action$ = wasFavorited
        ? this.interactionService.removeFromExternalFavorites(this.externalKey)
        : this.interactionService.addToExternalFavorites(this.externalKey, {
            title: m.title, author: m.author, genre: m.genre,
            imageUrl: m.imageUrl, type: m.type, description: m.description, releaseYear: m.releaseYear,
          });

      action$.subscribe({
        next: () => {
          this.interactionService.isExternalFavoritedFromBackend(this.externalKey).subscribe(favorited => {
            this.isFavorited.set(favorited);
            this.favoriteLoading.set(false);
            
            if (favorited) {
              this.recommendationService.likeExternalMedia(
                this.externalKey, 'FAVORITE', m.title, m.type ?? ''
              ).subscribe();
            } else {
              this.recommendationService.unlikeExternalMedia(this.externalKey, 'FAVORITE').subscribe();
            }
          });
        },
        error: () => {
          this.favoriteLoading.set(false);
        },
      });
      return;
    }

    this.favoriteLoading.set(true);
    const mediaId = this.media()!.id;
    const wasFavorited = this.isFavorited();

    const action$ = wasFavorited
      ? this.interactionService.removeFromFavorites(mediaId)
      : this.interactionService.addToFavorites(mediaId);

    action$.subscribe({
      next: () => {
        this.interactionService.isFavoritedFromBackend(mediaId).subscribe(favorited => {
          this.isFavorited.set(favorited);
          this.favoriteLoading.set(false);
        });
      },
      error: () => this.favoriteLoading.set(false),
    });
  }



  toggleWatchlist(): void {
    if (this.watchlistLoading() || !this.media()) return;

    if (this.isExternal) {
      this.watchlistLoading.set(true);
      const m = this.media()!;
      const action$ = this.isInWatchlist()
        ? this.interactionService.removeFromExternalWatchlist(this.externalKey)
        : this.interactionService.addToExternalWatchlist(this.externalKey, {
            title: m.title, author: m.author, genre: m.genre,
            imageUrl: m.imageUrl, type: m.type, description: m.description, releaseYear: m.releaseYear,
          });

      action$.subscribe({
        next: () => {
          this.interactionService.isInExternalWatchlistFromBackend(this.externalKey).subscribe(inWatchlist => {
            this.isInWatchlist.set(inWatchlist);
            this.watchlistLoading.set(false);
          });
        },
        error: () => this.watchlistLoading.set(false),
      });
      return;
    }

    this.watchlistLoading.set(true);
    const mediaId = this.media()!.id;
    const action$ = this.isInWatchlist()
      ? this.interactionService.removeFromWatchlist(mediaId)
      : this.interactionService.addToWatchlist(mediaId);

    action$.subscribe({
      next: () => {
        this.interactionService.isInWatchlistFromBackend(mediaId).subscribe(inWatchlist => {
          this.isInWatchlist.set(inWatchlist);
          this.watchlistLoading.set(false);
        });
      },
      error: () => this.watchlistLoading.set(false),
    });
  }


  setRating(star: number): void {
    this.userRating.set(star);
    if (!this.media()) return;
    if (this.isExternal) {
      this.saveExternalRating();
    } else {
      this.interactionService.saveRating(this.media()!.id, star);
    }
  }

  hoverRating(star: number): void      { this.hoveredRating.set(star); }
  clearHover(): void                   { this.hoveredRating.set(0); }
  isStarActive(star: number): boolean  { return star <= (this.hoveredRating() || this.userRating()); }

  get ratingLabel(): string {
    const labels = ['', 'Mauvais', 'Passable', 'Bien', 'Très bien', 'Excellent'];
    return labels[this.userRating()] ?? '';
  }


  submitComment(): void {
    if (!this.newComment.trim() || this.commentLoading() || !this.media()) return;

    if (this.isExternal) {
      this.commentLoading.set(true);
      const media = this.media()!;
      const ownerId = String(media.ownerId ?? '0');
      const mediaTitle = media.title;
      
      this.interactionService.postExternalComment(
        this.externalKey,
        this.newComment.trim(),
        this.userRating() || undefined,
        ownerId,
        mediaTitle
      ).subscribe({
        next: (comment) => {
          this.comments.update(list => [comment, ...list]);
          this.newComment = '';
          this.commentLoading.set(false);
        },
        error: () => this.commentLoading.set(false),
      });
      return;
    }

    this.commentLoading.set(true);
    const media = this.media()!;
    const ownerId = String(media.ownerId ?? '0');
    
    this.interactionService.postComment(
      media.id,
      this.newComment.trim(),
      this.userRating() || undefined,
      ownerId,
      media.title
    ).subscribe({
      next: (comment) => {
        this.comments.update(list => {
          const exists = list.some(c => c.id === comment.id);
          return exists ? list : [comment, ...list];
        });
        this.newComment = '';
        this.commentLoading.set(false);
      },
      error: () => this.commentLoading.set(false),
    });
  }

  deleteComment(commentId: number): void {
    if (!this.media()) return;

    if (this.isExternal) {
      this.interactionService.deleteExternalComment(this.externalKey, commentId).subscribe({
        next: () => this.comments.update(list => list.filter(c => c.id !== commentId)),
        error: () => {},
      });
      return;
    }

    this.interactionService.deleteComment(commentId, this.media()!.id).subscribe({
      next: () => this.comments.update(list => list.filter(c => c.id !== commentId)),
      error: () => {},
    });
  }


  getTypeLabel(type: string): string {
    const labels: Record<string, string> = { FILM: 'Film', BOOK: 'Livre', GAME: 'Jeu', PODCAST: 'Podcast' };
    return labels[type] ?? type;
  }

  getTypeBadgeClass(type: string): string {
    const classes: Record<string, string> = {
      FILM: 'bg-blue-100 text-blue-700', BOOK: 'bg-amber-100 text-amber-700',
      GAME: 'bg-purple-100 text-purple-700', PODCAST: 'bg-green-100 text-green-700',
    };
    return classes[type] ?? 'bg-gray-100 text-gray-600';
  }

  getStartLabel(type: string): string {
    const labels: Record<string, string> = {
      FILM: 'Regarder', BOOK: 'Lire', GAME: 'Jouer', PODCAST: 'Écouter',
    };
    return labels[type] ?? 'Commencer';
  }

  formatDate(d: string): string {
    return new Date(d).toLocaleDateString('fr-FR', { day: '2-digit', month: 'long', year: 'numeric' });
  }

  goBack(): void               { this.router.navigate(['/dashboard']); }
  navigateTo(id: number): void { this.router.navigate(['/media', id]); }
}