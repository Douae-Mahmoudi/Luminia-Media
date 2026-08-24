// src/app/features/dashboard/dashboard-admin/dashboard-admin.ts

import { Component, OnInit, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { AdminService } from '../../../core/services/admin';
import { AdminNotificationService, AdminNotificationResponse, NotificationStatsResponse, NotificationType, PageResponse } from '../../../core/services/admin-notification.service';
import { UserProfileDto } from '../../../core/models/auth.models';
import { MediaResponse, UpdateMediaRequest, MediaType, MediaStatus } from '../../../core/models/media.models';

@Component({
  selector: 'app-dashboard-admin',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
  ],
  templateUrl: './dashboard-admin.html',
})
export class DashboardAdminComponent implements OnInit {

  users           = signal<UserProfileDto[]>([]);
  allMedia        = signal<MediaResponse[]>([]);
  pendingMedia    = signal<MediaResponse[]>([]);
  loading         = signal(true);
  loadingMedia    = signal(false);
  loadingAllMedia = signal(false);
  error           = '';
  successMessage  = '';

  activeTab: 'users' | 'pending' | 'all' | 'notifications' = 'users';

  editingMedia: MediaResponse | null = null;
  editForm: UpdateMediaRequest = {
    title: '', author: '', type: 'FILM', genre: '',
    releaseYear: undefined, description: '', imageUrl: '', contentUrl: '',
  };
  mediaTypes: MediaType[] = ['FILM', 'BOOK', 'GAME', 'PODCAST'];

  notifStats       = signal<NotificationStatsResponse | null>(null);
  notifPage        = signal<PageResponse<AdminNotificationResponse> | null>(null);
  loadingNotifs    = signal(false);
  loadingStats     = signal(false);
  notifCurrentPage = signal(0);
  notifTypeFilter  = signal<string>('');
  broadcastMsg     = signal('');
  broadcasting     = signal(false);
  broadcastSent    = signal(false);

  readonly notifTypes: NotificationType[] = [
    'LIKE', 'COMMENT', 'FAVORITE', 'WATCHLIST',
    'MEDIA_APPROVED', 'MEDIA_REJECTED', 'BROADCAST', 'SYSTEM',
  ];

  constructor(
    public  auth:         AuthService,
    private admin:        AdminService,
    private notifService: AdminNotificationService,
    public  router:       Router,
    private cdr:          ChangeDetectorRef,
  ) {}

  ngOnInit() {
    this.loadUsers();
    this.loadPendingMedia();
    this.loadAllMedia();
  }

  switchTab(tab: 'users' | 'pending' | 'all' | 'notifications') {
    this.activeTab = tab;
    if (tab === 'notifications' && !this.notifStats()) {
      this.loadNotifStats();
      this.loadNotifications(0);
    }
  }

  get adminCount() { return this.users().filter(u => u.role === 'ADMIN').length; }
  get userCount()  { return this.users().filter(u => u.role === 'USER').length; }

  loadUsers() {
    this.loading.set(true);
    this.admin.getAllUsers().subscribe({
      next: (users) => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: () => {
        this.error = 'Error loading users.';
        this.loading.set(false);
      },
    });
  }

  toggleRole(user: UserProfileDto) {
    const newRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN';
    this.admin.updateUserRole(user.id, newRole).subscribe({
      next: (updated) => {
        this.users.update(list => list.map(u => u.id === updated.id ? updated : u));
        this.showSuccess(`Role updated for ${updated.firstName} ${updated.lastName}`);
      },
      error: () => { this.error = 'Error updating role.'; },
    });
  }

  loadPendingMedia() {
    this.loadingMedia.set(true);
    this.admin.getPendingMedia().subscribe({
      next:  (items) => { this.pendingMedia.set(items); this.loadingMedia.set(false); },
      error: ()      => { this.loadingMedia.set(false); this.error = 'Error loading pending media.'; },
    });
  }

  approve(id: number) {
    this.admin.approveMedia(id).subscribe({
      next: (m) => {
        this.pendingMedia.update(list => list.filter(x => x.id !== id));
        this.loadAllMedia();
        this.showSuccess(`"${m.title}" approved!`);
      },
      error: () => { this.error = 'Error approving media.'; },
    });
  }

  reject(id: number) {
    this.admin.rejectMedia(id).subscribe({
      next: (m) => {
        this.pendingMedia.update(list => list.filter(x => x.id !== id));
        this.loadAllMedia();
        this.showSuccess(`"${m.title}" rejected.`);
      },
      error: () => { this.error = 'Error rejecting media.'; },
    });
  }

  loadAllMedia() {
    this.loadingAllMedia.set(true);
    this.admin.getAllMedia().subscribe({
      next:  (items) => { this.allMedia.set(items); this.loadingAllMedia.set(false); },
      error: ()      => { this.loadingAllMedia.set(false); this.error = 'Error loading media.'; },
    });
  }

  startEdit(media: MediaResponse) {
    this.editingMedia = media;
    this.editForm = {
      title: media.title, author: media.author, type: media.type,
      genre: media.genre || '', releaseYear: media.releaseYear,
      description: media.description || '', imageUrl: media.imageUrl || '',
      contentUrl: media.contentUrl || '',
    };
  }

  cancelEdit() {
    this.editingMedia = null;
    this.editForm = {
      title: '', author: '', type: 'FILM', genre: '',
      releaseYear: undefined, description: '', imageUrl: '', contentUrl: '',
    };
  }

  saveEdit() {
    if (!this.editingMedia) return;
    const req: UpdateMediaRequest = {};
    if (this.editForm.title?.trim())       req.title       = this.editForm.title.trim();
    if (this.editForm.author?.trim())      req.author      = this.editForm.author.trim();
    if (this.editForm.type)                req.type        = this.editForm.type;
    if (this.editForm.genre?.trim())       req.genre       = this.editForm.genre.trim();
    if (this.editForm.releaseYear)         req.releaseYear = this.editForm.releaseYear;
    if (this.editForm.description?.trim()) req.description = this.editForm.description.trim();
    if (this.editForm.imageUrl?.trim())    req.imageUrl    = this.editForm.imageUrl.trim();
    if (this.editForm.contentUrl?.trim())  req.contentUrl  = this.editForm.contentUrl.trim();

    this.loadingAllMedia.set(true);
    this.admin.updateMedia(this.editingMedia.id, req).subscribe({
      next: (updated) => {
        this.allMedia.update(list => {
          const idx = list.findIndex(m => m.id === updated.id);
          if (idx !== -1) {
            const arr = [...list];
            arr[idx] = updated;
            return arr;
          }
          return list;
        });
        if (!this.allMedia().find(m => m.id === updated.id)) {
          this.loadAllMedia();
        }
        this.cancelEdit();
        this.loadingAllMedia.set(false);
        this.showSuccess(`"${updated.title}" updated!`);
      },
      error: () => { this.loadingAllMedia.set(false); this.error = 'Error updating media.'; },
    });
  }

  deleteMedia(id: number, title: string) {
    if (!confirm(`Delete "${title}"?`)) return;
    this.loadingAllMedia.set(true);
    this.admin.deleteMedia(id).subscribe({
      next: () => {
        this.allMedia.update(list => list.filter(m => m.id !== id));
        this.pendingMedia.update(list => list.filter(m => m.id !== id));
        this.loadingAllMedia.set(false);
        this.showSuccess(`"${title}" deleted.`);
      },
      error: () => { this.loadingAllMedia.set(false); this.error = 'Error deleting media.'; },
    });
  }

  loadNotifStats() {
    this.loadingStats.set(true);
    this.notifService.getStats().subscribe({
      next:  (s) => { this.notifStats.set(s); this.loadingStats.set(false); },
      error: ()  => this.loadingStats.set(false),
    });
  }

  loadNotifications(page = 0) {
    this.loadingNotifs.set(true);
    const typeFilter = this.notifTypeFilter();
    this.notifService.getAll({
      type: typeFilter ? typeFilter as NotificationType : undefined,
      page,
      size: 20,
    }).subscribe({
      next: (res) => {
        this.notifPage.set(res);
        this.notifCurrentPage.set(page);
        this.loadingNotifs.set(false);
      },
      error: () => this.loadingNotifs.set(false),
    });
  }

  onTypeFilterChange() {
    this.loadNotifications(0);
  }

  sendBroadcast() {
    const msg = this.broadcastMsg().trim();
    if (!msg) return;
    this.broadcasting.set(true);
    this.notifService.broadcast({ message: msg, type: 'BROADCAST' }).subscribe({
      next: () => {
        this.broadcasting.set(false);
        this.broadcastSent.set(true);
        this.broadcastMsg.set('');
        setTimeout(() => this.broadcastSent.set(false), 3000);
        this.loadNotifications(0);
      },
      error: () => this.broadcasting.set(false),
    });
  }

  showSuccess(msg: string) {
    this.successMessage = msg;
    setTimeout(() => this.successMessage = '', 3000);
  }

  clearError() { this.error = ''; }
  logout()     { this.auth.logout(); }

  getTypeClass(type: string): string {
    const m: Record<string, string> = {
      FILM:    'bg-blue-100 text-blue-700',
      BOOK:    'bg-amber-100 text-amber-700',
      GAME:    'bg-purple-100 text-purple-700',
      PODCAST: 'bg-green-100 text-green-700',
    };
    return m[type] || 'bg-gray-100 text-gray-700';
  }

  getStatusClass(status: MediaStatus): string {
    const m: Record<MediaStatus, string> = {
      AVAILABLE:   'bg-green-100 text-green-700',
      PENDING:     'bg-yellow-100 text-yellow-700',
      REJECTED:    'bg-red-100 text-red-700',
      UNAVAILABLE: 'bg-gray-100 text-gray-700',
    };
    return m[status];
  }

  getStatusText(status: MediaStatus): string {
    const m: Record<MediaStatus, string> = {
      AVAILABLE:   'Available',
      PENDING:     'Pending',
      REJECTED:    'Rejected',
      UNAVAILABLE: 'Unavailable',
    };
    return m[status];
  }

  getNotifTypeBadge(type: string): string {
    const m: Record<string, string> = {
      LIKE:           'bg-red-100 text-red-600',
      COMMENT:        'bg-blue-100 text-blue-600',
      FAVORITE:       'bg-amber-100 text-amber-600',
      WATCHLIST:      'bg-indigo-100 text-indigo-600',
      MEDIA_APPROVED: 'bg-green-100 text-green-700',
      MEDIA_REJECTED: 'bg-red-100 text-red-700',
      BROADCAST:      'bg-purple-100 text-purple-700',
      SYSTEM:         'bg-gray-100 text-gray-600',
    };
    return m[type] ?? 'bg-gray-100 text-gray-600';
  }

  formatDate(d: string): string {
    return new Date(d).toLocaleDateString('fr-FR', {
      day: '2-digit', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });
  }

  get countByTypeEntries(): { key: string; value: number }[] {
    const stats = this.notifStats();
    if (!stats?.countByType) return [];
    return Object.entries(stats.countByType).map(([key, value]) => ({ key, value }));
  }
}