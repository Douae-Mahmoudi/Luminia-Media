 
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
 
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.html',
})
export class LoginComponent {
  email = '';
  password = '';
  error = '';
  loading = false;
 
  constructor(private auth: AuthService, private router: Router) {}
 
  submit() {
    this.error = '';
    this.loading = true;
    this.auth.login({ email: this.email.trim(), password: this.password }).subscribe({
      next: (res) => {
        this.loading = false;
        this.router.navigate([res.role === 'ADMIN' ? '/admin/dashboard' : '/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.message || 'Email ou mot de passe incorrect.';
      },
    });
  }
}
 










