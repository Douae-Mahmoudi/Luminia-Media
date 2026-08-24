import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd } from '@angular/router';
import { Subscription, merge, filter } from 'rxjs';
import { NotificationService, NotificationResponse } from '../../core/services/notification.service';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notifications.html',
})
export class NotificationsComponent implements OnInit, OnDestroy {

  notifications = signal<NotificationResponse[]>([]);
  loading       = signal<boolean>(true);
  currentPage   = signal<number>(0);
  totalPages    = signal<number>(1);
  unreadCount   = signal<number>(0);

  readonly pageSize = 20;

  private subs = new Subscription();

  constructor(
    private notificationService: NotificationService,
    public  router: Router,
  ) {}

  ngOnInit(): void {
    this.loadAll();

    this.subs.add(
      merge(
        this.notificationService.userChanged$,
        this.router.events.pipe(
          filter(e => e instanceof NavigationEnd),
          filter((e: NavigationEnd) => e.urlAfterRedirects.includes('/notifications')),
        ),
      ).subscribe(() => this.loadAll()),
    );
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  private loadAll(): void {
    this.notifications.set([]);   
    this.unreadCount.set(0);
    this.currentPage.set(0);
    this.loadNotifications(0);
    this.loadBadge();
  }

  loadNotifications(page = 0): void {
    this.loading.set(true);
    this.notificationService.getNotifications(page, this.pageSize).subscribe({
      next: (res) => {
        this.notifications.set(res.content);
        this.totalPages.set(res.totalPages);
        this.currentPage.set(res.number);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  loadBadge(): void {
    this.notificationService.getBadgeCount().subscribe({
      next: (res) => this.unreadCount.set(res.count),
      error: () => {},
    });
  }

  markOneAsRead(id: number): void {
    const notif = this.notifications().find(n => n.id === id);
    if (notif?.read) return;
    this.notificationService.markOneAsRead(id).subscribe({
      next: () => {
        this.notifications.update(list =>
          list.map(n => n.id === id ? { ...n, read: true } : n)
        );
        this.unreadCount.update(c => Math.max(0, c - 1));
      },
    });
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications.update(list => list.map(n => ({ ...n, read: true })));
        this.unreadCount.set(0);
      },
    });
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages()) return;
    this.loadNotifications(page);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  formatDate(date: string): string {
    const d    = new Date(date);
    const now  = new Date();
    const diff = Math.floor((now.getTime() - d.getTime()) / 1000);
    if (diff < 60)     return 'À l\'instant';
    if (diff < 3600)   return `Il y a ${Math.floor(diff / 60)} min`;
    if (diff < 86400)  return `Il y a ${Math.floor(diff / 3600)} h`;
    if (diff < 604800) return `Il y a ${Math.floor(diff / 86400)} j`;
    return d.toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
  }

  get unreadNotifications(): NotificationResponse[] {
    return this.notifications().filter(n => !n.read);
  }

  get readNotifications(): NotificationResponse[] {
    return this.notifications().filter(n => n.read);
  }
}