import { Component } from '@angular/core';
import { UserService } from './service/user.service';
import { ProjectService } from './service/project.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'Proper';

  // Ver que projectService deberia cambiarse para que realmente traiga los proyectos
  // y no los workflows
  constructor(private userService: UserService,
              private projectService: ProjectService) {}
  
}