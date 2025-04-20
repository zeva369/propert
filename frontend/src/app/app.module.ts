import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
// import { RouterModule } from '@angular/router';
import { AppComponent } from './app.component';
import { NavbarComponent } from './navbar/navbar.component';
import { HomeComponent } from './home/home.component';
import { ExamplesComponent } from './examples/examples.component';

import { HttpClientModule } from '@angular/common/http';
import { PertchartComponent } from './pertchart/pertchart.component';
import { TaskEditorComponent } from './task-editor/task-editor.component';
import { FormsModule } from '@angular/forms';
import { EditorComponent } from './editor/editor.component';
import { ProjectSelectorComponent } from './project-selector/project-selector.component';
import { UserService } from './service/user.service';
import { AuthService } from './service/auth.service';
import { TaskService } from './service/task.service';
import { ProjectService } from './service/project.service';
import { WorkflowService } from './service/workflow.service';
import { CommonModule } from '@angular/common';


@NgModule({
  declarations: [
    AppComponent,
    NavbarComponent,
    HomeComponent,
    ExamplesComponent,
    PertchartComponent,
    TaskEditorComponent,
    EditorComponent,
    ProjectSelectorComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    CommonModule,
    // RouterModule
  ],
  providers: [
    AuthService,
    ProjectService,
    TaskService,
    UserService,
    WorkflowService,
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
