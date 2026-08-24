import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../../core/services/auth';
import { UserProfileDto } from '../../core/models/auth.models';
 
@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './profile.component.html',
})
export class ProfileComponent implements OnInit {
  private readonly PROFILE_API = 'http://localhost:8081/auth';
 
  profile = signal<UserProfileDto | null>(null);
  loading = signal(true);
  saving  = signal(false);
  success = signal(false);
  error   = signal('');
 
  activeTab: 'profile' | 'security' | 'support' = 'profile';
 
  passwordForm = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  };
  passwordSaving = signal(false);
  passwordSuccess = signal(false);
  passwordError = signal('');
 
  form = {
    firstName: '',
    lastName:  '',
    email: '',
    phone: '',
    bio: ''
  };
 
  constructor(
    public  auth:   AuthService,
    private http:   HttpClient,
    private router: Router,
  ) {}
 
  ngOnInit(): void {
    this.loadProfile();
  }
 
  private getHeaders(): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${this.auth.getToken()}` });
  }
 
  loadProfile(): void {
    this.loading.set(true);
    this.error.set('');
    this.http.get<UserProfileDto>(`${this.PROFILE_API}/me`, { headers: this.getHeaders() })
      .subscribe({
        next: (p) => {
          this.profile.set(p);
          this.form = {
            firstName: p.firstName ?? '',
            lastName:  p.lastName  ?? '',
            email:     p.email     ?? '',
            phone:     p.phone     ?? '',
            bio:       p.bio       ?? ''
          };
          this.loading.set(false);
        },
        error: () => {
          this.error.set('Impossible de charger le profil.');
          this.loading.set(false);
        },
      });
  }
 
  save(): void {
    this.saving.set(true);
    this.success.set(false);
    this.error.set('');
    this.http.patch<UserProfileDto>(`${this.PROFILE_API}/me`, {
      firstName: this.form.firstName,
      lastName:  this.form.lastName,
      phone:     this.form.phone,
      bio:       this.form.bio
    }, { headers: this.getHeaders() })
      .subscribe({
        next: (updated) => {
          this.profile.set(updated);
          this.saving.set(false);
          this.success.set(true);
          setTimeout(() => this.success.set(false), 3000);
        },
        error: () => {
          this.error.set('Erreur lors de la mise à jour.');
          this.saving.set(false);
        },
      });
  }
 
  changePassword(): void {
    if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
      this.passwordError.set('Les mots de passe ne correspondent pas.');
      return;
    }
    if (this.passwordForm.newPassword.length < 6) {
      this.passwordError.set('Le mot de passe doit contenir au moins 6 caractères.');
      return;
    }
 
    this.passwordSaving.set(true);
    this.passwordError.set('');
    this.passwordSuccess.set(false);
 
    this.http.post(`${this.PROFILE_API}/me/change-password`, {
      currentPassword: this.passwordForm.currentPassword,
      newPassword:     this.passwordForm.newPassword
    }, { headers: this.getHeaders() }).subscribe({
      next: () => {
        this.passwordSaving.set(false);
        this.passwordSuccess.set(true);
        this.passwordForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
        setTimeout(() => this.passwordSuccess.set(false), 3000);
      },
      error: (err) => {
        this.passwordSaving.set(false);
        this.passwordError.set(err.error?.message || 'Erreur lors du changement de mot de passe.');
      }
    });
  }
 
  goBack(): void {
    this.router.navigate([this.auth.getRole() === 'ADMIN' ? '/admin' : '/dashboard']);
  }
 
  getInitials(): string {
    const p = this.profile();
    if (!p) return '?';
    return `${(p.firstName?.charAt(0) || '').toUpperCase()}${(p.lastName?.charAt(0) || '').toUpperCase()}`;
  }
}
 


















