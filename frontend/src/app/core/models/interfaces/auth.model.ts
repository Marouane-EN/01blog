import { UserDto } from "./user.model";

export interface LoginRequest {
  identifier: string;
  password: string;
}

export interface RegistrationRequest {
  username: string;
  email: string;
  password: string;
  bio?: string;
  birthDate: Date;
}

export interface AuthResponse {
  token: string;
  userProfile: UserDto;
}