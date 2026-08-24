import { Component, EventEmitter, Output, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders, HttpEventType } from '@angular/common/http';
import { AuthService } from '../../core/services/auth';
 
export interface CreateMediaRequest {
  title:         string;
  author:        string;
  description:   string;
  type:          'FILM' | 'BOOK' | 'GAME' | 'PODCAST';
  releaseYear:   number | null;
  genre:         string;
  imageUrl:      string;
  contentUrl:    string;
  ownerUsername: string;
}
 
type InputMode = 'url' | 'file';
 
@Component({
  selector: 'app-submit-media-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './submit-media-modal.component.html',
})
export class SubmitMediaModalComponent {
 
  @Output() closed    = new EventEmitter<void>();
  @Output() submitted = new EventEmitter<void>();
 
  private http        = inject(HttpClient);
  private authService = inject(AuthService);
 
  private readonly BASE = 'http://localhost:8082';
  private readonly AUTH = 'http://localhost:8081';
 
  loading  = signal(false);
  success  = signal(false);
  errorMsg = signal('');
 
  imageMod   = signal<InputMode>('url');
  contentMod = signal<InputMode>('url');
 
  imageProgress   = signal<number>(0);
  contentProgress = signal<number>(0);
 
  imageFileName   = signal<string>('');
  contentFileName = signal<string>('');
 
  readonly types: { value: 'FILM' | 'BOOK' | 'GAME' | 'PODCAST'; label: string; icon: string }[] = [
    { value: 'FILM',    label: 'Film',    icon: '🎬' },
    { value: 'BOOK',    label: 'Livre',   icon: '📚' },
    { value: 'GAME',    label: 'Jeu',     icon: '🎮' },
    { value: 'PODCAST', label: 'Podcast', icon: '🎙️' },
  ];
 
  form: CreateMediaRequest = {
    title:         '',
    author:        '',
    description:   '',
    type:          'FILM',
    releaseYear:   null,
    genre:         '',
    imageUrl:      '',
    contentUrl:    '',
    ownerUsername: '',
  };
 
  get contentAccept(): string {
    switch (this.form.type) {
      case 'FILM':    return 'video/mp4,video/mkv,video/webm,video/avi,video/quicktime';
      case 'BOOK':    return 'application/pdf';
      case 'PODCAST': return 'audio/mpeg,audio/mp3,audio/wav,audio/x-m4a,audio/ogg,audio/aac';
      case 'GAME':    return '';
    }
  }
 
  get contentPlaceholder(): string {
    const map: Record<string, string> = {
      FILM: 'https://… (lien streaming)', BOOK: 'https://… (lien PDF)',
      PODCAST: 'https://… (lien audio)',  GAME: 'https://… (lien du jeu)',
    };
    return map[this.form.type] ?? 'https://…';
  }
 
  private get token(): string {
    return this.authService.getToken() ?? localStorage.getItem('token') ?? '';
  }
 
  private get username(): string {
    return this.authService.getUsername() ?? '';
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
          localStorage.setItem('userId', String(id));
          return Number(id);
        }
      } catch {}
    }
    return null;
  }
 
  private isBase64Image(url: string): boolean {
    return url?.startsWith('data:image/') || url?.startsWith('data:application/');
  }
 
  private isUrlTooLong(url: string): boolean {
    return url?.length > 255;
  }
 
  onImageFileChange(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.imageFileName.set(file.name);
    this.uploadFile(file, 'image');
  }
 
  onContentFileChange(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.contentFileName.set(file.name);
    this.uploadFile(file, 'content');
  }
 
  private uploadFile(file: File, target: 'image' | 'content'): void {
    const formData = new FormData();
    formData.append('file', file);
 
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${this.token}` });
 
    this.http.post(`${this.BASE}/api/upload`, formData, {
      headers,
      reportProgress: true,
      observe: 'events',
    }).subscribe({
      next: (event) => {
        if (event.type === HttpEventType.UploadProgress && event.total) {
          const pct = Math.round(100 * event.loaded / event.total);
          target === 'image'
            ? this.imageProgress.set(pct)
            : this.contentProgress.set(pct);
        }
        if (event.type === HttpEventType.Response) {
          const url = (event.body as any)?.url ?? '';
          const fullUrl = `${this.BASE}${url}`;
          if (target === 'image') {
            this.form.imageUrl = fullUrl;
            this.imageProgress.set(100);
          } else {
            this.form.contentUrl = fullUrl;
            this.contentProgress.set(100);
          }
        }
      },
      error: () => {
        this.errorMsg.set(`Erreur lors de l'upload ${target === 'image' ? 'de l\'image' : 'du contenu'}.`);
        target === 'image' ? this.imageProgress.set(0) : this.contentProgress.set(0);
      },
    });
  }
 
  selectType(type: 'FILM' | 'BOOK' | 'GAME' | 'PODCAST'): void {
    this.form.type = type;
    this.form.contentUrl    = '';
    this.contentMod.set('url');
    this.contentProgress.set(0);
    this.contentFileName.set('');
  }
 
  submit(): void {
    if (!this.form.title.trim() || !this.form.author.trim()) {
      this.errorMsg.set('Le titre et l\'auteur sont obligatoires.');
      return;
    }
 
    if (this.isBase64Image(this.form.imageUrl)) {
      this.errorMsg.set('Les images en base64 ne sont pas acceptées. Utilise un lien HTTP (ex: https://...) ou upload un fichier via le bouton "Fichier".');
      return;
    }
 
    if (this.isUrlTooLong(this.form.imageUrl)) {
      this.errorMsg.set('Le lien de l\'image est trop long (max 255 caractères). Utilise un lien plus court ou upload un fichier.');
      return;
    }
 
    if (this.isUrlTooLong(this.form.contentUrl)) {
      this.errorMsg.set('Le lien du contenu est trop long (max 255 caractères). Utilise un lien plus court ou upload un fichier.');
      return;
    }
 
    this.loading.set(true);
    this.errorMsg.set('');
 
    const userId = this.resolvedUserId;
    if (userId !== null) {
      this.doSubmit(userId);
    } else {
      const authHeaders = new HttpHeaders({ 'Authorization': `Bearer ${this.token}` });
      this.http.get<{ id: number }>(`${this.AUTH}/auth/me`, { headers: authHeaders })
        .subscribe({
          next: (profile) => {
            if (!profile.id || isNaN(profile.id)) {
              this.loading.set(false);
              this.errorMsg.set('Impossible d\'identifier l\'utilisateur. Reconnectez-vous.');
              return;
            }
            localStorage.setItem('userId', String(profile.id));
            this.doSubmit(profile.id);
          },
          error: () => {
            this.loading.set(false);
            this.errorMsg.set('Impossible d\'identifier l\'utilisateur. Reconnectez-vous.');
          },
        });
    }
  }
 
  private doSubmit(userId: number): void {
    const headers = new HttpHeaders({
      'Content-Type':  'application/json',
      'Authorization': `Bearer ${this.token}`,
      'X-User-Id':     String(userId),
    });
 
    const body = {
      ...this.form,
      ownerId:       userId,
      ownerUsername: this.username,
      releaseYear:   this.form.releaseYear ? Number(this.form.releaseYear) : null,
    };
 
    this.http.post(`${this.BASE}/api/media/submit`, body, { headers }).subscribe({
      next: () => {
        this.loading.set(false);
        this.success.set(true);
        setTimeout(() => {
          this.success.set(false);
          this.submitted.emit();
          this.close();
        }, 1500);
      },
      error: (err) => {
        this.loading.set(false);
        const msg = err?.error?.message ?? err?.error ?? 'Erreur lors de la soumission.';
        this.errorMsg.set(typeof msg === 'string' ? msg : JSON.stringify(msg));
      },
    });
  }
 
  close(): void { this.closed.emit(); }
 
  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal-backdrop')) {
      this.close();
    }
  }
}
 




























