 
export interface LoginRequest {
  email: string;
  password: string;
}
 
export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}
 
export interface AuthResponse {
  token: string | null;
  role: string | null;
  message: string;
}
 
export interface ForgotPasswordRequest {
  email: string;
}
 
export interface VerifyCodeRequest {
  email: string;
  code: string;
}
 
export interface ResetPasswordRequest {
  email: string;
  code: string;
  newPassword: string;
}
 
export interface UserProfileDto {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
  phone?: string;      
  bio?: string;        
  createdAt?: string;
  updatedAt?: string;

}
 










