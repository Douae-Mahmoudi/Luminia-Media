 
export interface Collection {
  id:          string;
  userId:      string;
  name:        string;
  description: string;
  isPublic:    boolean;
  mediaIds:    string[];
  mediaCount:  number;
  createdAt:   string;
  updatedAt:   string;
}
 
export interface CreateCollectionInput {
  name:        string;
  description: string;
  isPublic:    boolean;
}
