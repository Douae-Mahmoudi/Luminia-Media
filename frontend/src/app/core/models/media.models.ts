export type MediaType = 'FILM' | 'BOOK' | 'GAME' | 'PODCAST';
 
export type MediaStatus = 'PENDING' | 'AVAILABLE' | 'REJECTED' | 'UNAVAILABLE';
 
export interface MediaResponse {
  id: number;
  title: string;
  author: string;
  description?: string;
  type: MediaType;
  status: MediaStatus;
  releaseYear?: number;
  genre?: string;
  imageUrl?: string;
  contentUrl?: string;
  createdAt?: string;
  updatedAt?: string;
  ownerId?: number;
  ownerUsername?: string;
  externalId?: string;
  source?: string;  
}
 
export interface UpdateMediaRequest {
  title?: string;
  author?: string;
  description?: string;
  type?: MediaType;
  releaseYear?: number;
  genre?: string;
  imageUrl?: string;
  contentUrl?: string;
}
 
export interface ExternalMediaResponse {
  title: string;
  author: string;
  genre?: string;
  releaseYear?: number;
  description?: string;
  coverUrl?: string;
  readUrl?: string;
  source?: string;
  externalId?: string;
}
 
