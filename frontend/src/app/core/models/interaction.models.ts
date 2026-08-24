export interface UserMediaData {
  favorited:   boolean;
  inWatchlist: boolean;
  userRating:  number;
  comments:    Comment[];
  liked:       boolean;      
  likesCount:  number;       
}


export interface Comment {
  id:        number;
  username:  string;   
  userId:    string;   
  content:   string;
  rating?:   number;
  createdAt: string;
}

export type UserLocalData = Record<number, UserMediaData>;