 
import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../../core/services/auth';
import { MediaResponse } from '../../core/models/media.models';
import { SubmitMediaModalComponent } from '../../components/submit-media-modal/submit-media-modal.component';

import {
  NotificationService,
  NotificationResponse,
} from '../../core/services/notification.service';
 
@Component({
  selector: 'app-my-media',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, SubmitMediaModalComponent],
  templateUrl: './my-media.component.html',
})
export class MyMediaComponent implements OnInit {
 
  items     = signal<MediaResponse[]>([]);
  loading   = signal(true);
  error     = signal('');
  username  = '';
  skeletons = Array(9).fill(0);
 
  activeFilter: 'ALL' | 'AVAILABLE' | 'PENDING' | 'REJECTED' = 'ALL';
  sortOrder:    'recent' | 'alpha' | 'type' | 'status'        = 'recent';
  viewMode:     'grid'  | 'list'                              = 'grid';
 
  showNotifPanel  = signal(false);
  showSubmitModal = signal(false);
  notifications  = signal<NotificationResponse[]>([]);
  loadingNotifs  = signal(false);
  unreadCount    = signal(0);
 
  router              = inject(Router);        
  private http        = inject(HttpClient);
  private authService = inject(AuthService);
  private notifSvc    = inject(NotificationService);
 
  private readonly BASE = 'http://localhost:8082';
  private readonly AUTH = 'http://localhost:8081';
 
  private get token(): string {
    return this.authService.getToken() ?? '';
  }
 
  private get resolvedUserId(): number | null {
    const fromService = this.authService.getUserId();
    if (fromService != null && !isNaN(fromService) && fromService > 0) return fromService;
 
    const fromStorage = localStorage.getItem('userId');
    if (fromStorage) {
      const parsed = Number(fromStorage);
      if (!isNaN(parsed) && parsed > 0) return parsed;
    }
 
    const token = this.token;
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const id = payload.id ?? payload.userId ?? null;
        if (id != null && !isNaN(Number(id)) && Number(id) > 0) {
          const numId = Number(id);
          localStorage.setItem('userId', String(numId));
          return numId;
        }
      } catch {}
    }
    return null;
  }
 
  ngOnInit(): void {
    this.username = this.authService.getUsername() ?? '';
    this.load();
    this.loadNotifications();
  }
 
  load(): void {
    this.loading.set(true);
    this.error.set('');
 
    const token = this.token;
    if (!token) {
      this.error.set('Veuillez vous reconnecter.');
      this.loading.set(false);
      return;
    }
 
    const userId = this.resolvedUserId;
    if (userId !== null) {
      this.fetchMedia(token, userId);
      return;
    }
 
    const authHeaders = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    this.http.get<{ id: number }>(`${this.AUTH}/auth/me`, { headers: authHeaders }).subscribe({
      next: (profile) => {
        if (!profile.id || isNaN(profile.id)) {
          this.error.set('Identifiant utilisateur invalide. Veuillez vous reconnecter.');
          this.loading.set(false);
          return;
        }
        localStorage.setItem('userId', String(profile.id));
        this.fetchMedia(token, profile.id);
      },
      error: () => {
        this.error.set("Impossible d'identifier l'utilisateur. Veuillez vous reconnecter.");
        this.loading.set(false);
      },
    });
  }
 
  private fetchMedia(token: string, userId: number): void {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'X-User-Id':     String(userId),
    });
 
    this.http.get<MediaResponse[]>(`${this.BASE}/api/media/my`, { headers }).subscribe({
      next: (data) => {
        const order: Record<string, number> = { PENDING: 0, AVAILABLE: 1, REJECTED: 2 };
        this.items.set(
          [...data].sort((a, b) => (order[a.status] ?? 3) - (order[b.status] ?? 3))
        );
        this.loading.set(false);
      },
      error: (err) => {
        console.error('[MyMedia] HTTP error', err.status, err.error);
        this.error.set(
          err.status === 403 ? 'Accès refusé. Vérifiez que vous êtes bien connecté.'
          : err.status === 400 ? 'Identifiant utilisateur invalide. Veuillez vous reconnecter.'
          : 'Impossible de charger vos médias. Vérifiez votre connexion.'
        );
        this.loading.set(false);
      },
    });
  }
 
  loadNotifications(): void {
    this.loadingNotifs.set(true);
 
    this.notifSvc.getBadgeCount().subscribe({
      next: (res) => this.unreadCount.set(res.count),
      error: ()   => this.unreadCount.set(0),
    });
 
    this.notifSvc.getNotifications(0, 20).subscribe({
      next: (page) => {
        this.notifications.set(page.content);
        this.loadingNotifs.set(false);
      },
      error: () => {
        this.loadingNotifs.set(false);
      },
    });
  }
 
  toggleNotifPanel(): void {
    this.showNotifPanel.update(v => !v);
    if (this.showNotifPanel()) {
      this.loadNotifications();
    }
  }
 
  closeNotifPanel(): void {
    this.showNotifPanel.set(false);
  }
 
  markAllAsRead(): void {
    this.notifications.update(list => list.map(n => ({ ...n, read: true })));
    this.unreadCount.set(0);
 
    this.notifSvc.markAllAsRead().subscribe({
      error: () => this.loadNotifications(), 
    });
  }
 
  markOneAsRead(id: number): void {
    const already = this.notifications().find(n => n.id === id)?.read;
    if (already) return;
 
    this.notifications.update(list =>
      list.map(n => n.id === id ? { ...n, read: true } : n)
    );
    this.unreadCount.update(c => Math.max(0, c - 1));
 
    this.notifSvc.markOneAsRead(id).subscribe({ error: () => {} });
  }
 
  formatNotifDate(dateStr: string): string {
    if (!dateStr) return '';
    const d    = new Date(dateStr);
    const diff = Math.floor((Date.now() - d.getTime()) / 60000);
    if (diff < 1)  return "À l'instant";
    if (diff < 60) return `Il y a ${diff} min`;
    const h = Math.floor(diff / 60);
    if (h < 24)    return `Il y a ${h}h`;
    const dd = Math.floor(h / 24);
    if (dd < 7)    return `Il y a ${dd}j`;
    return d.toLocaleDateString('fr-FR', { day: '2-digit', month: 'short' });
  }
 
  filteredItems(): MediaResponse[] {
    let list = this.items();
 
    if (this.activeFilter !== 'ALL') {
      list = list.filter(i => i.status === this.activeFilter);
    }
 
    switch (this.sortOrder) {
      case 'alpha':
        list = [...list].sort((a, b) => a.title.localeCompare(b.title));
        break;
      case 'type':
        list = [...list].sort((a, b) => a.type.localeCompare(b.type));
        break;
      case 'status': {
        const ord: Record<string, number> = { AVAILABLE: 0, PENDING: 1, REJECTED: 2 };
        list = [...list].sort((a, b) => (ord[a.status] ?? 9) - (ord[b.status] ?? 9));
        break;
      }
      default: break;
    }
    return list;
  }
 
  navigate(item: MediaResponse): void {
    if (item.status === 'AVAILABLE') this.router.navigate(['/media', item.id]);
  }
 
  goToDashboard():     void { this.router.navigate(['/dashboard']); }
  goToMyMedia():       void { this.router.navigate(['/my-media']); }
  goToCollections():   void { this.router.navigate(['/collections']); }
  goToWatchlist():     void { this.router.navigate(['/watchlist']); }
  goToFavorites():     void { this.router.navigate(['/favorites']); }
  goToProfile():       void { this.router.navigate(['/profile']); }
  goToNotifications(): void { this.closeNotifPanel(); this.router.navigate(['/notifications']); }
  openSubmitModal():  void { this.showSubmitModal.set(true); }
  closeSubmitModal(): void { this.showSubmitModal.set(false); }
  onMediaSubmitted(): void {
    this.showSubmitModal.set(false);
    this.load();
  }
 
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
 
  statusLabel(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'En attente', AVAILABLE: 'Publié', REJECTED: 'Refusé',
    };
    return map[status] ?? status;
  }
 
  typeLabel(type: string): string {
    const map: Record<string, string> = {
      FILM: 'Film', BOOK: 'Livre', GAME: 'Jeu', PODCAST: 'Podcast',
    };
    return map[type] ?? type;
  }
 
  formatDate(d: string | undefined): string {
    if (!d) return '';
    return new Date(d).toLocaleDateString('fr-FR', {
      day: '2-digit', month: 'long', year: 'numeric',
    });
  }
 
  get pendingCount():   number { return this.items().filter(i => i.status === 'PENDING').length; }
  get publishedCount(): number { return this.items().filter(i => i.status === 'AVAILABLE').length; }
  get rejectedCount():  number { return this.items().filter(i => i.status === 'REJECTED').length; }
}