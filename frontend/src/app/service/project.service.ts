import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpStatusCode } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, Observable, throwError } from 'rxjs';
import { UserService } from './user.service';
import { Project } from '../entity/project';
// import { Task } from './task';

const API_RESOURCE_ENDPOINT = 'http://127.0.0.1:8080/propert-backend/api/v1/authenticated/projects';

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  
  constructor(private readonly httpClient: HttpClient, 
              private readonly router: Router) {
    
  }

  public getProjects(userId : string): Observable<Project[]> {
    
    if (userId == "guest") return new Observable<Project[]>(subscriber => {
      subscriber.next([new Project(0, "My Project", "",[])]);
    });

    //!!!Notar que la variable headers es inmutable, si no re reasigna el resultado de append
    //la variable no es modificada
    let headers = new HttpHeaders();
    headers = headers.append('Accept', 'application/json');
    // headers = headers.append('Authorization', 'Bearer ' + localStorage.getItem('jwtToken'));

    const httpOptions = {
      headers: headers
    };

    //Lo que figura en el diamante es en realidad un cast
    return this.httpClient.get<Project[]>(API_RESOURCE_ENDPOINT + "/userId=" + encodeURI(userId), httpOptions)
    .pipe(
       catchError((e) => {
        console.info(this.router.url)
       // this.router.navigate(['/login'], {queryParams:{prevPage:this.router.url}})
        // console.error('user not fully authenticated');
        return throwError(() => new Error(e.error.message))
      })
    )
  }
}
