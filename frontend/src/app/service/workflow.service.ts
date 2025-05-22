import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpStatusCode } from '@angular/common/http';
import { Router } from '@angular/router';
import { Workflow } from '../entity/workflow';
import { catchError, Observable, throwError } from 'rxjs';
import { Predecessor } from '../entity/predecessor';
import { Task } from '../entity/task';


const API_RESOURCE_ENDPOINT = 'http://127.0.0.1:8080/propert-backend/api/v1/guest/projects';

@Injectable({
  providedIn: 'root'
})
export class WorkflowService {
  
  constructor(private readonly httpClient: HttpClient, 
              private readonly router :Router) {
    
  }

  public getWorkFlow(tasks: Task[]): Observable<Workflow> {
    
    //!!!Notar que la variable headers es inmutable, si no re reasigna el resultado de append
    //la variable no es modificada
    let headers = new HttpHeaders();
    headers = headers.append('Accept', 'application/json');
    // headers = headers.append('Authorization', 'Bearer ' + localStorage.getItem('jwtToken'));

    const httpOptions = {
      headers: headers
    };

    const tasksForApi = tasks.map(task => ({
      ...task,
      predecessors: task.predecessors.map((p: Predecessor)=> p.id) // solo los ids
    }));

    //Lo que figura en el diamante es en realidad un cast
    return this.httpClient.post<Workflow>(API_RESOURCE_ENDPOINT, tasksForApi, httpOptions)
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
