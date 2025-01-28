import { Injectable } from '@angular/core';
import { UserService } from './user.service';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { Task } from '../entity/task';

const API_RESOURCE_ENDPOINT = 'http://127.0.0.1:8080/propert-backend/api/v1/authenticated/tasks'

@Injectable({
  providedIn: 'root',
})
export class TaskService {
  private storageKey = 'guestTasks';

  constructor(private userService: UserService, 
              private http: HttpClient) {}

  getTasks(projectId : bigint): Observable<Task[]> {
    if (this.userService.isGuest()) {
      const tasks = sessionStorage.getItem(this.storageKey);
      return of(tasks ? JSON.parse(tasks) : []);
    } else {
      let headers = new HttpHeaders();
      headers = headers.append('Accept', 'application/json');
 
      const httpOptions = {
        headers: headers
      };
      return this.http.get<Task[]>(API_RESOURCE_ENDPOINT, httpOptions);
    }
  }

  saveTasks(tasks: any[]): void {
    if (this.userService.isGuest()) {
      sessionStorage.setItem(this.storageKey, JSON.stringify(tasks));
    }
    //  else {
    //   this.http.post('https://api.example.com/tasks', tasks).subscribe();
    // }
  }
}
