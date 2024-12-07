import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
// import { RouterModule } from '@angular/router';
import { AppComponent } from './app.component';
import { NavbarComponent } from './navbar/navbar.component';
import { HomeComponent } from './home/home.component';
import { ExamplesComponent } from './examples/examples.component';
import { ProjectService } from './project.service';
import { HttpClientModule } from '@angular/common/http';
import { PertchartComponent } from './pertchart/pertchart.component';
import { TaskEditorComponent } from './task-editor/task-editor.component';
import { FormsModule } from '@angular/forms';
import { EditorComponent } from './editor/editor.component';
import { ProjectSelectorComponent } from './project-selector/project-selector.component';


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
    FormsModule
    // RouterModule
  ],
  providers: [ProjectService],
  bootstrap: [AppComponent]
})
export class AppModule { }
