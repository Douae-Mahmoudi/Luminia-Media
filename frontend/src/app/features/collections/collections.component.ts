import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CollectionService } from '../../core/services/collection.service';
import { AuthService } from '../../core/services/auth';
import { Collection, CreateCollectionInput } from '../../core/models/collection.models';

@Component({
  selector: 'app-collections',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './collections.component.html',
})
export class CollectionsComponent implements OnInit {

  collections           = signal<Collection[]>([]);
  otherPublicCollections = signal<Collection[]>([]);
  loading               = signal<boolean>(true);
  showForm              = signal<boolean>(false);
  saving                = signal<boolean>(false);
  deletingId            = signal<string | null>(null);
  showDeleteConfirm     = signal<string | null>(null);

  privateCollections = computed(() => this.collections().filter(c => !c.isPublic));
  publicCollections  = computed(() => this.collections().filter(c => c.isPublic));

  form: CreateCollectionInput = { name: '', description: '', isPublic: false };

  constructor(
    private collectionService: CollectionService,
    private auth:              AuthService,
    public  router:            Router,
  ) {}

  ngOnInit(): void {
    this.load();
    this.loadAllPublic();
  }

  load(): void {
    const userId = this.auth.getUsername() ?? '';
    this.loading.set(true);
    this.collectionService.getByUserId(userId).subscribe({
      next:  (cols) => { this.collections.set(cols); this.loading.set(false); },
      error: ()     => this.loading.set(false),
    });
  }

  loadAllPublic(): void {
    const currentUserId = this.auth.getUsername() ?? '';
    this.collectionService.getAllPublic().subscribe({
      next: (cols) => {
        this.otherPublicCollections.set(cols.filter(c => c.userId !== currentUserId));
      },
      error: () => {},
    });
  }

  openForm(): void {
    this.form = { name: '', description: '', isPublic: false };
    this.showForm.set(true);
  }

  closeForm(): void { this.showForm.set(false); }

  togglePublic(): void {
    this.form = { ...this.form, isPublic: !this.form.isPublic };
  }

  submit(): void {
    if (!this.form.name.trim()) return;
    this.saving.set(true);
    this.collectionService.create(this.form).subscribe({
      next: (col) => {
        if (col.id) this.collections.update(list => [col, ...list]);
        this.saving.set(false);
        this.showForm.set(false);
      },
      error: () => this.saving.set(false),
    });
  }

  goToCollection(id: string): void {
    this.router.navigate(['/collections', id]);
  }

  confirmDelete(event: MouseEvent, id: string): void {
    event.stopPropagation();
    this.showDeleteConfirm.set(id);
  }

  cancelDelete(): void {
    this.showDeleteConfirm.set(null);
  }

  deleteCollection(id: string): void {
    this.deletingId.set(id);
    this.collectionService.delete(id).subscribe({
      next: () => {
        this.collections.update(list => list.filter(c => c.id !== id));
        this.deletingId.set(null);
        this.showDeleteConfirm.set(null);
      },
      error: () => {
        this.deletingId.set(null);
        this.showDeleteConfirm.set(null);
      },
    });
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}