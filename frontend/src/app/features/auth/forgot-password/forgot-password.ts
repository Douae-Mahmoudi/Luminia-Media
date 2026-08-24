 
import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
 
@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './forgot-password.html',
  host: { ngSkipHydration: 'true' }
})
export class ForgotPasswordComponent {
  email = '';
  code = '';
  newPassword = '';
  confirmPassword = '';
 
  step = signal<number>(1);
  loading = signal<boolean>(false);
  error = signal<string>('');
  success = signal<string>('');
 
  constructor(private auth: AuthService, private router: Router) {}
 
  sendCode() {
    if (!this.email.trim()) return;
    this.error.set('');
    this.loading.set(true);
    this.auth.forgotPassword({ email: this.email.trim() }).subscribe({
      next: () => { this.loading.set(false); this.step.set(2); },
      error: () => { this.loading.set(false); this.error.set('Email introuvable.'); },
    });
  }
 
  verifyCode() {
    if (!this.code.trim()) return;
    this.error.set('');
    this.loading.set(true);
    this.auth.verifyCode({ email: this.email.trim(), code: this.code.trim() }).subscribe({
      next: () => { this.loading.set(false); this.step.set(3); },
      error: () => { this.loading.set(false); this.error.set('Code invalide ou expiré.'); },
    });
  }
 
  resetPassword() {
    this.error.set('');
    if (this.newPassword !== this.confirmPassword) {
      this.error.set('Les mots de passe ne correspondent pas.');
      return;
    }
    this.loading.set(true);
    this.auth.resetPassword({
      email: this.email.trim(),
      code: this.code.trim(),
      newPassword: this.newPassword,
    }).subscribe({
      next: () => {
        this.loading.set(false);
        this.success.set('Mot de passe réinitialisé ! Redirection...');
        setTimeout(() => this.router.navigate(['/login']), 1500);
      },
      error: () => { this.loading.set(false); this.error.set('Erreur lors de la réinitialisation.'); },
    });
  }
 
  onCodeInput(event: Event) {
    const input = event.target as HTMLInputElement;
    this.code = input.value.replace(/\D/g, '').slice(0, 6);
    input.value = this.code;
  }
}
 








