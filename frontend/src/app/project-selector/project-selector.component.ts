import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Project } from '../project';

@Component({
  selector: 'app-project-selector',
  templateUrl: './project-selector.component.html',
  styleUrls: ['./project-selector.component.css']
})
export class ProjectSelectorComponent {
  @Output() currentProjectChanged = new EventEmitter<Project>();
  @Input() projects : Project[] = [];

  
}
