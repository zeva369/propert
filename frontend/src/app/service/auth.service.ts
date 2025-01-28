import { Injectable } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private tokenKey = 'jwtToken';

  constructor(private cookieService: CookieService) {}

  setToken(token: string): void {
    this.cookieService.set(this.tokenKey, token, { 
      path: '/', 
      secure: true, 
      sameSite: 'Lax' // Puedes cambiar esto según tus necesidades
    });
  }

  getToken(): string | null {
    return this.cookieService.get(this.tokenKey);
  }

  clearToken(): void {
    this.cookieService.delete(this.tokenKey, '/');
  }
}