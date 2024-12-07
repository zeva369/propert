import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { User } from './user';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private storageKey = 'currentUser';

  constructor(private http: HttpClient) {}

  setUser(user: User): void {
    sessionStorage.setItem(this.storageKey, JSON.stringify(user));
  }

  getUser(): User | null {
    const user = sessionStorage.getItem(this.storageKey);
    return user ? JSON.parse(user) : null;
  }

  isGuest(): boolean {
    const user = this.getUser();
    return user?.isGuest ?? true;
  }

  clearUser(): void {
    sessionStorage.removeItem(this.storageKey);
  }

  // // Para usuarios autenticados, carga los datos desde la API
  // fetchUserFromApi(): Observable<{ id: string; name: string; isGuest: boolean }> {
  //   return this.http.get<User>('https://api.example.com/user');
  // }
}