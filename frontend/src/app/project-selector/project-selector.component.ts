import { Component, EventEmitter, Input, Output, SimpleChanges } from '@angular/core';
import { Project } from '../entity/project';

@Component({
  selector: 'app-project-selector',
  templateUrl: './project-selector.component.html',
  styleUrls: ['./project-selector.component.css']
})
export class ProjectSelectorComponent {
  @Output() currentProjectChanged = new EventEmitter<Project>();
  @Input() projects: Project[] = [];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['projects']) {
      this.updateProjectList();
    }
  }

  public updateProjectList() {
    
  }

}
