import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { Project } from '../entity/project';

@Component({
  selector: 'app-project-selector',
  templateUrl: './project-selector.component.html',
  styleUrls: ['./project-selector.component.css']
})
export class ProjectSelectorComponent implements OnChanges {
  @Output() currentProjectChanged = new EventEmitter<Project>();
  @Input() projects: Project[] = [];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['projects']) {
      this.updateProjectList();
    }
  }

  public updateProjectList() {
    // Logic to update the project list   
  }

}
