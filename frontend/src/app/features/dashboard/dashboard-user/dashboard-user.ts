import { Component, OnInit, OnDestroy, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { forkJoin, Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, filter } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth';
import { MediaService } from '../../../core/services/media.service';
import { InteractionService } from '../../../core/services/interaction.service';
import { RecommendationService, ExternalTopics } from '../../../core/services/recommendation.service';
import { NotificationService, NotificationResponse, BadgeCountResponse } from '../../../core/services/notification.service';
import { MediaResponse, ExternalMediaResponse, MediaType } from '../../../core/models/media.models';
import { SubmitMediaModalComponent } from '../../../components/submit-media-modal/submit-media-modal.component';
 
type Tab           = 'all' | 'FILM' | 'BOOK' | 'GAME' | 'PODCAST';
type GameSource    = 'all' | 'rawg' | 'free';
type PodcastSource = 'all' | 'index' | 'itunes';
 
export interface MediaCard {
  id?:          number;
  title:        string;
  author:       string;
  genre?:       string;
  imageUrl?:    string;
  type?:        MediaType;
  source?:      string;
  description?: string;
  releaseYear?: number;
  readUrl?:     string;
  externalId?:  string;
  ownerId?:     number;
}
 
const PAGE_SIZE = 12;
 
@Component({
  selector: 'app-dashboard-user',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, SubmitMediaModalComponent],
  templateUrl: './dashboard-user.html',
})
export class DashboardUserComponent implements OnInit, OnDestroy {
 
  readonly Math = Math;
 
  showSubmitModal = signal<boolean>(false);
  unreadCount     = signal<number>(0);
  showNotifPanel  = signal<boolean>(false);
  notifications   = signal<NotificationResponse[]>([]);
  loadingNotifs   = signal<boolean>(false);
 
  openSubmitModal(): void  { this.showSubmitModal.set(true);  }
  closeSubmitModal(): void { this.showSubmitModal.set(false); }
  onMediaSubmitted(): void {
    this.closeSubmitModal();
    this.loadRecommendations();
  }
 
  activeTab     = signal<Tab>('all');
  gameFilter    = signal<GameSource>('all');
  podcastFilter = signal<PodcastSource>('all');
 
  searchQuery   = '';
  searchResults = signal<ExternalMediaResponse[]>([]);
  searching     = signal<boolean>(false);
  private searchSubject = new Subject<string>();
 
  recommended     = signal<MediaResponse[]>([]);
  loadingReco     = signal<boolean>(true);
  loadingContinue = signal<boolean>(true);
  showAllReco     = signal<boolean>(false);
 
  recoIndex = signal<number>(0);
  private autoPlayInterval: any = null;
  private readonly AUTO_PLAY_DELAY = 5000;
 
  canRecoNext = computed<boolean>(() => this.recoIndex() + 3 < this.recommended().length);
  canRecoPrev = computed<boolean>(() => this.recoIndex() > 0);
 
  currentPage       = signal<number>(1);
  readonly pageSize = PAGE_SIZE;
 
  private allCards  = signal<MediaCard[]>([]);
  private routerSub!: Subscription;
 
  private likedExternalIds = new Set<string>();
  private likedInternalIds = new Set<number>();
  private likesLoaded      = false;
 
  filteredCards = computed<MediaCard[]>(() => {
    const tab = this.activeTab();
    const gf  = this.gameFilter();
    const pf  = this.podcastFilter();
    let cards = this.allCards();
    if (tab !== 'all') cards = cards.filter(c => c.type === tab);
    if (tab === 'GAME' && gf !== 'all') {
      const sourceMap: Record<GameSource, string[]> = { all: [], rawg: ['RAWG'], free: ['FreeToGame'] };
      cards = cards.filter(c => sourceMap[gf].includes(c.source ?? '') || c.source === 'internal');
    }
    if (tab === 'PODCAST' && pf !== 'all') {
      if (pf === 'index')  cards = cards.filter(c => c.source === 'PodcastIndex');
      if (pf === 'itunes') cards = cards.filter(c => c.source === 'iTunes');
    }
    return cards;
  });
 
  totalPages  = computed<number>(() => Math.max(1, Math.ceil(this.filteredCards().length / this.pageSize)));
  pagedCards  = computed<MediaCard[]>(() => {
    const start = (this.currentPage() - 1) * this.pageSize;
    return this.filteredCards().slice(start, start + this.pageSize);
  });
  pageEnd     = computed<number>(() => Math.min(this.currentPage() * this.pageSize, this.filteredCards().length));
  pageNumbers = computed<number[]>(() => {
    const total = this.totalPages();
    const cur   = this.currentPage();
    const pages: number[] = [];
    for (let i = 1; i <= total; i++) {
      if (i === 1 || i === total || (i >= cur - 2 && i <= cur + 2)) pages.push(i);
    }
    return pages;
  });
 
  filterTabs: { label: string; type: Tab }[] = [
    { label: 'Tous',     type: 'all'     },
    { label: 'Films',    type: 'FILM'    },
    { label: 'Livres',   type: 'BOOK'    },
    { label: 'Jeux',     type: 'GAME'    },
    { label: 'Podcasts', type: 'PODCAST' },
  ];
 
  constructor(
    public  auth:                  AuthService,
    public  router:                Router,
    private mediaService:          MediaService,
    private interactionService:    InteractionService,
    private recommendationService: RecommendationService,
    private notificationService:   NotificationService,
  ) {}
 
  ngOnInit(): void {
    this.loadUnreadCount();
 
    this.recommendationService.likedInfo().subscribe({
      next: (info) => {
        this.likedExternalIds.clear();
        this.likedInternalIds.clear();
        (info.likedExternalIds || []).forEach((id: string) => this.likedExternalIds.add(id));
        (info.likedInternalIds || []).forEach((id: number) => this.likedInternalIds.add(id));
        this.likesLoaded = true;
        this.loadContinueWatchingWithTopics();
        this.loadRecommendations();
      },
      error: () => {
        this.likesLoaded = true;
        this.loadContinueWatchingWithTopics();
        this.loadRecommendations();
      },
    });
 
    this.routerSub = this.router.events.pipe(
      filter(e => e instanceof NavigationEnd),
    ).subscribe(() => this.loadRecommendations());
 
    this.searchSubject.pipe(
      debounceTime(400),
      distinctUntilChanged(),
    ).subscribe(q => this.runSearch(q));
 
    this.startAutoPlay();
  }
 
  ngOnDestroy(): void {
    this.routerSub?.unsubscribe();
    this.stopAutoPlay();
  }
 
  startAutoPlay(): void {
    if (this.autoPlayInterval) this.stopAutoPlay();
    this.autoPlayInterval = setInterval(() => {
      if (!this.showAllReco() && this.canRecoNext()) {
        this.recoNext();
      }
    }, this.AUTO_PLAY_DELAY);
  }
 
  stopAutoPlay(): void {
    if (this.autoPlayInterval) {
      clearInterval(this.autoPlayInterval);
      this.autoPlayInterval = null;
    }
  }
 
  resetAutoPlay(): void {
    this.stopAutoPlay();
    this.startAutoPlay();
  }
 
  recoNext(): void {
    const max = Math.max(0, this.recommended().length - 3);
    this.recoIndex.update(i => Math.min(i + 1, max));
    this.resetAutoPlay();
  }
 
  recoPrev(): void {
    this.recoIndex.update(i => Math.max(0, i - 1));
    this.resetAutoPlay();
  }
 
  resetCarousel(): void {
    this.recoIndex.set(0);
  }
 
  toggleShowAllReco(): void {
    this.showAllReco.set(!this.showAllReco());
    if (this.showAllReco()) {
      this.stopAutoPlay();
    } else {
      this.startAutoPlay();
      this.resetCarousel();
    }
  }
 
  pauseAutoPlay(): void { this.stopAutoPlay(); }
 
  resumeAutoPlay(): void {
    if (!this.showAllReco()) this.startAutoPlay();
  }
 
  loadUnreadCount(): void {
    this.notificationService.getBadgeCount().subscribe({
      next: (res: BadgeCountResponse) => this.unreadCount.set(res.count),
      error: (err) => console.error('[Dashboard] Erreur badge:', err),
    });
  }
 
  toggleNotifPanel(): void {
    if (!this.showNotifPanel()) this.loadNotifications();
    this.showNotifPanel.update(v => !v);
  }
 
  closeNotifPanel(): void { this.showNotifPanel.set(false); }
 
  loadNotifications(): void {
    this.loadingNotifs.set(true);
    this.notificationService.getNotifications().subscribe({
      next: (page) => {
        this.notifications.set(page.content);
        this.loadingNotifs.set(false);
      },
      error: (err) => {
        console.error('[Dashboard] Erreur chargement notifications:', err);
        this.loadingNotifs.set(false);
      },
    });
  }
 
  markOneAsRead(id: number): void {
    this.notificationService.markOneAsRead(id).subscribe({
      next: () => {
        this.notifications.update(list => list.map(n => n.id === id ? { ...n, read: true } : n));
        this.unreadCount.update(c => Math.max(0, c - 1));
      },
      error: (err) => console.error('[Dashboard] Erreur markOneAsRead:', err),
    });
  }
 
  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications.update(list => list.map(n => ({ ...n, read: true })));
        this.unreadCount.set(0);
      },
      error: (err) => console.error('[Dashboard] Erreur markAllAsRead:', err),
    });
  }
 
  formatNotifDate(date: string): string {
    const d    = new Date(date);
    const now  = new Date();
    const diff = Math.floor((now.getTime() - d.getTime()) / 1000);
    if (diff < 60)    return 'À l\'instant';
    if (diff < 3600)  return `${Math.floor(diff / 60)} min`;
    if (diff < 86400) return `${Math.floor(diff / 3600)} h`;
    return d.toLocaleDateString('fr-FR', { day: '2-digit', month: 'short' });
  }
 
  goToDetail(id: number): void { this.router.navigate(['/media', id]); }
 
  goToExternalDetail(item: MediaCard): void {
    let resolvedType = item.type;
    if (!resolvedType && item.externalId) {
      const match = item.externalId.match(/^EXT_(FILM|BOOK|GAME|PODCAST)_/);
      if (match) resolvedType = match[1] as MediaType;
    }
    this.router.navigate(['/media/external'], {
      state: {
        title: item.title, author: item.author, genre: item.genre,
        coverUrl: item.imageUrl, readUrl: item.readUrl, type: resolvedType,
        source: item.source, description: item.description,
        releaseYear: item.releaseYear, externalId: item.externalId,
      },
    });
  }
 
  navigateCard(item: MediaCard): void {
    item.id ? this.goToDetail(item.id) : this.goToExternalDetail(item);
  }
 
  likeExternal(item: MediaCard, likeType: 'LIKE' | 'FAVORITE' = 'LIKE'): void {
    if (!item.externalId) return;
    this.recommendationService.likeExternalMedia(
      item.externalId, likeType, item.title ?? '', item.type ?? '',
    ).subscribe({
      next: (freshTopics) => {
        this.likedExternalIds.add(item.externalId!);
        this.reloadWithFreshTopics(freshTopics);
      },
    });
  }
 
  likeInternal(item: MediaCard): void {
    if (!item.id) return;
    const ownerId = String(item.ownerId ?? '0');
    this.interactionService.likeMedia(item.id, item.title ?? '', ownerId).subscribe({
      next: () => {
        this.likedInternalIds.add(item.id!);
        this.recommended.update(list => list.filter(r => r.id !== item.id));
        this.loadRecommendations();
      },
      error: (err: unknown) => console.error('[Dashboard] like local erreur', err),
    });
  }
 
  private reloadWithFreshTopics(topics: ExternalTopics): void {
    this.loadingContinue.set(true);
    this.loadingReco.set(true);
    this.showAllReco.set(false);
    this.resetCarousel();
 
    const filmTopic     = topics.FILM?.[0]    ?? 'popular';
    const bookTopic     = topics.BOOK?.[0]    ?? 'popular';
    const bookTopic2    = topics.BOOK?.[1]    ?? 'best';
    const gameTopic     = topics.GAME?.[0]    ?? 'popular';
    const podcastTopic  = topics.PODCAST?.[0] ?? 'technology';
    const podcastTopic2 = topics.PODCAST?.[1] ?? 'technology';
 
    forkJoin({
      local:          this.mediaService.getAvailable(),
      filmsTvmaze:    this.mediaService.searchExternalFilms(filmTopic),
      filmsYoutube:   this.mediaService.searchExternalFilmsYoutube(filmTopic),
      filmsArchive:   this.mediaService.searchExternalFilmsArchive(filmTopic),
      booksPopular:   this.mediaService.searchExternalBooks(bookTopic),
      booksBest:      this.mediaService.searchExternalBooks(bookTopic2),
      gamesRawg:      this.mediaService.searchExternalGames(gameTopic),
      gamesFree:      this.mediaService.searchExternalFreeGames(''),
      podcastsIndex:  this.mediaService.searchExternalPodcastsIndex(podcastTopic),
      podcastsItunes: this.mediaService.searchExternalPodcastsItunes(podcastTopic2),
    }).subscribe({
      next: (data) => this.processCards(data),
      error: () => this.loadingContinue.set(false),
    });
 
    this.recommendationService.getEnrichedRecommendations().subscribe({
      next: (res) => {
        this.recommended.set(this._filterLiked(res.recommendations));
        this.loadingReco.set(false);
      },
      error: () => this.loadingReco.set(false),
    });
  }
 
  private buildExternalId(type: MediaType, item: ExternalMediaResponse): string {
    const key = item.externalId ? item.externalId : encodeURIComponent((item.title ?? 'unknown').trim().toLowerCase());
    return `EXT_${type}_${key}`;
  }
 
  private _filterLiked(recommendations: MediaResponse[]): MediaResponse[] {
    return recommendations.filter(r => {
      if (r.externalId) return !this.likedExternalIds.has(r.externalId);
      if (r.id && r.id > 0) return !this.likedInternalIds.has(r.id);
      return true;
    });
  }
 
  loadRecommendations(): void {
    this.loadingReco.set(true);
    this.showAllReco.set(false);
    this.resetCarousel();
 
    this.recommendationService.getEnrichedRecommendations().subscribe({
      next: (res) => {
        const withoutBooks = res.recommendations.filter(r => r.type !== 'BOOK');
        this.recommended.set(this._filterLiked(withoutBooks));
        this.loadingReco.set(false);
        this.startAutoPlay();
      },
      error: () => {
        this.mediaService.getAvailable().subscribe({
          next: (items) => {
            const withoutBooks = items.filter(r => r.type !== 'BOOK');
            this.recommended.set(withoutBooks.filter(r => !this.likedInternalIds.has(r.id)));
            this.loadingReco.set(false);
            this.startAutoPlay();
          },
          error: () => this.loadingReco.set(false),
        });
      },
    });
  }
 
  loadContinueWatchingWithTopics(): void {
    this.loadingContinue.set(true);
    this.recommendationService.getExternalTopics().subscribe({
      next: (topics) => {
        const filmTopic     = topics.FILM?.[0]    ?? 'popular';
        const bookTopic     = topics.BOOK?.[0]    ?? 'popular';
        const bookTopic2    = topics.BOOK?.[1]    ?? 'best';
        const gameTopic     = topics.GAME?.[0]    ?? 'popular';
        const podcastTopic  = topics.PODCAST?.[0] ?? 'technology';
        const podcastTopic2 = topics.PODCAST?.[1] ?? 'technology';
 
        forkJoin({
          local:          this.mediaService.getAvailable(),
          filmsTvmaze:    this.mediaService.searchExternalFilms(filmTopic),
          filmsYoutube:   this.mediaService.searchExternalFilmsYoutube(filmTopic),
          filmsArchive:   this.mediaService.searchExternalFilmsArchive(filmTopic),
          booksPopular:   this.mediaService.searchExternalBooks(bookTopic),
          booksBest:      this.mediaService.searchExternalBooks(bookTopic2),
          gamesRawg:      this.mediaService.searchExternalGames(gameTopic),
          gamesFree:      this.mediaService.searchExternalFreeGames(''),
          podcastsIndex:  this.mediaService.searchExternalPodcastsIndex(podcastTopic),
          podcastsItunes: this.mediaService.searchExternalPodcastsItunes(podcastTopic2),
        }).subscribe({
          next: (data) => this.processCards(data),
          error: () => this.loadingContinue.set(false),
        });
      },
      error: () => this.loadContinueWatching(),
    });
  }
 
  loadContinueWatching(): void {
    this.loadingContinue.set(true);
    forkJoin({
      local:          this.mediaService.getAvailable(),
      filmsTvmaze:    this.mediaService.searchExternalFilms('popular'),
      filmsYoutube:   this.mediaService.searchExternalFilmsYoutube('popular'),
      filmsArchive:   this.mediaService.searchExternalFilmsArchive('popular'),
      booksPopular:   this.mediaService.searchExternalBooks('popular'),
      booksBest:      this.mediaService.searchExternalBooks('best'),
      gamesRawg:      this.mediaService.searchExternalGames('popular'),
      gamesFree:      this.mediaService.searchExternalFreeGames(''),
      podcastsIndex:  this.mediaService.searchExternalPodcastsIndex('technology'),
      podcastsItunes: this.mediaService.searchExternalPodcastsItunes('technology'),
    }).subscribe({
      next: (data) => this.processCards(data),
      error: () => this.loadingContinue.set(false),
    });
  }
 
  private processCards(data: any): void {
    const { local, filmsTvmaze, filmsYoutube, filmsArchive, booksPopular, booksBest, gamesRawg, gamesFree, podcastsIndex, podcastsItunes } = data;
    const cards: MediaCard[] = [];
 
    const toCard = (item: ExternalMediaResponse, type: MediaType): MediaCard => ({
      title: item.title, author: item.author, genre: item.genre,
      imageUrl: item.coverUrl, type, source: item.source,
      description: item.description, releaseYear: item.releaseYear,
      readUrl: item.readUrl, externalId: this.buildExternalId(type, item),
    });
 
    local.forEach((item: MediaResponse) => cards.push({
      id: item.id, title: item.title, author: item.author, genre: item.genre,
      imageUrl: item.imageUrl, type: item.type, source: 'internal',
      description: item.description, releaseYear: item.releaseYear,
      readUrl: item.contentUrl, ownerId: item.ownerId,
    }));
 
    const filmCards = [
      ...filmsTvmaze.slice(0, 15).map((i: ExternalMediaResponse) => toCard(i, 'FILM')),
      ...filmsYoutube.slice(0, 15).map((i: ExternalMediaResponse) => toCard(i, 'FILM')),
      ...filmsArchive.slice(0, 15).map((i: ExternalMediaResponse) => toCard(i, 'FILM')),
    ];
    const bookCards = [
      ...booksPopular.slice(0, 15).map((i: ExternalMediaResponse) => toCard(i, 'BOOK')),
      ...booksBest.slice(0, 15).map((i: ExternalMediaResponse) => toCard(i, 'BOOK')),
    ];
    const gameCards = [
      ...gamesRawg.slice(0, 15).map((i: ExternalMediaResponse) => toCard(i, 'GAME')),
      ...gamesFree.slice(0, 15).map((i: ExternalMediaResponse) => toCard(i, 'GAME')),
    ];
    const podcastCards = [
      ...podcastsIndex.slice(0, 15).map((i: ExternalMediaResponse) => toCard(i, 'PODCAST')),
      ...podcastsItunes.slice(0, 15).map((i: ExternalMediaResponse) => toCard(i, 'PODCAST')),
    ];
 
    const grouped = [filmCards, bookCards, gameCards, podcastCards];
    const maxLen = Math.max(...grouped.map(g => g.length));
    for (let i = 0; i < maxLen; i++) {
      for (const group of grouped) { if (group[i]) cards.push(group[i]); }
    }
 
    const seen = new Set<string>();
    const unique = cards.filter(c => {
      const key = c.externalId ?? `${c.type}-${c.title?.toLowerCase()}`;
      if (c.externalId && this.likedExternalIds.has(c.externalId)) return false;
      if (seen.has(key)) return false;
      seen.add(key);
      return true;
    });
 
    this.allCards.set(unique);
    this.currentPage.set(1);
    this.loadingContinue.set(false);
  }
 
  selectTab(tab: Tab): void {
    this.activeTab.set(tab);
    this.gameFilter.set('all');
    this.podcastFilter.set('all');
    this.currentPage.set(1);
  }
 
  setGameFilter(f: GameSource): void       { this.gameFilter.set(f);    this.currentPage.set(1); }
  setPodcastFilter(f: PodcastSource): void { this.podcastFilter.set(f); this.currentPage.set(1); }
 
  goToPage(page: number): void {
    if (page < 1 || page > this.totalPages()) return;
    this.currentPage.set(page);
    document.getElementById('discover-section')?.scrollIntoView({ behavior: 'smooth' });
  }
 
  onSearch(): void { this.searchSubject.next(this.searchQuery.trim()); }
 
  private runSearch(q: string): void {
    if (!q) { this.searchResults.set([]); this.searching.set(false); return; }
    this.searching.set(true);
    forkJoin({
      films:          this.mediaService.searchExternalFilms(q),
      filmsYoutube:   this.mediaService.searchExternalFilmsYoutube(q),
      filmsArchive:   this.mediaService.searchExternalFilmsArchive(q),
      books:          this.mediaService.searchExternalBooks(q),
      games:          this.mediaService.searchExternalGames(q),
      gamesFree:      this.mediaService.searchExternalFreeGames(q),
      podcastsIndex:  this.mediaService.searchExternalPodcastsIndex(q),
      podcastsItunes: this.mediaService.searchExternalPodcastsItunes(q),
    }).subscribe({
      next: ({ films, filmsYoutube, filmsArchive, books, games, gamesFree, podcastsIndex, podcastsItunes }) => {
        const addId = (items: ExternalMediaResponse[], type: MediaType) =>
          items.map(i => ({ ...i, externalId: this.buildExternalId(type, i) }));
        const all = [
          ...addId(films, 'FILM'), ...addId(filmsYoutube, 'FILM'), ...addId(filmsArchive, 'FILM'),
          ...addId(books, 'BOOK'), ...addId(games, 'GAME'), ...addId(gamesFree, 'GAME'),
          ...addId(podcastsIndex, 'PODCAST'), ...addId(podcastsItunes, 'PODCAST'),
        ];
        const seen = new Set<string>();
        this.searchResults.set(all.filter(i => {
          const key = i.externalId ?? `${i.source}-${i.title?.toLowerCase()}`;
          if (seen.has(key)) return false;
          seen.add(key);
          return true;
        }));
        this.searching.set(false);
      },
      error: () => this.searching.set(false),
    });
  }
 
  clearSearch(): void {
    this.searchQuery = '';
    this.searchResults.set([]);
    this.searching.set(false);
  }
 
  getTypeLabel(type: string): string {
    return ({ FILM: 'Film', BOOK: 'Livre', GAME: 'Jeu', PODCAST: 'Podcast' } as Record<string, string>)[type] ?? type;
  }
 
  getTypeBadgeClass(type: string): string {
    return ({
      FILM: 'bg-blue-100 text-blue-700', BOOK: 'bg-amber-100 text-amber-700',
      GAME: 'bg-purple-100 text-purple-700', PODCAST: 'bg-green-100 text-green-700',
    } as Record<string, string>)[type] ?? 'bg-gray-100 text-gray-600';
  }
 
  goBack(): void { this.router.navigate(['/dashboard']); }
  logout(): void { this.auth.logout(); }
}
