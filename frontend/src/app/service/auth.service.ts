import { Injectable } from '@angular/core';
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly tokenKey = 'jwtToken';

  constructor() {}

  setToken(token: string): void {
    const date = new Date();
    date.setTime(date.getTime() + 7 * 24 * 60 * 60 * 1000); // Establece la cookie para 7 días
    const expires = `expires=${date.toUTCString()}`;
    document.cookie = `${this.tokenKey}=${token}; ${expires}; path=/; secure; samesite=lax`;
  }

  getToken(): string | null {
    const nameEQ = `${this.tokenKey}=`;
    const cookies = document.cookie.split(';');
    for (let i = 0; i < cookies.length; i++) {
      let cookie = cookies[i].trim();
      if (cookie.indexOf(nameEQ) === 0) {
        return cookie.substring(nameEQ.length, cookie.length);
      }
    }
    return null;
  }

  clearToken(): void {
    document.cookie = `${this.tokenKey}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; secure; samesite=lax`;
  }
}