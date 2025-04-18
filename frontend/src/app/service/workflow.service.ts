import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpStatusCode } from '@angular/common/http';
import { Router } from '@angular/router';
import { Workflow } from '../entity/workflow';
import { catchError, Observable, throwError } from 'rxjs';


const API_RESOURCE_ENDPOINT = 'http://127.0.0.1:8080/propert-backend/api/v1/guest/projects';

@Injectable({
  providedIn: 'root'
})
export class WorkflowService {
  
  constructor(private readonly httpClient: HttpClient, 
              private readonly router :Router) {
    
  }

  public getWorkFlow(tasks:unknown): Observable<Workflow> {
    
    //!!!Notar que la variable headers es inmutable, si no re reasigna el resultado de append
    //la variable no es modificada
    let headers = new HttpHeaders();
    headers = headers.append('Accept', 'application/json');
    // headers = headers.append('Authorization', 'Bearer ' + localStorage.getItem('jwtToken'));

    const httpOptions = {
      headers: headers
    };

    //Lo que figura en el diamante es en realidad un cast
    return this.httpClient.post<Workflow>(API_RESOURCE_ENDPOINT, tasks, httpOptions)
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
