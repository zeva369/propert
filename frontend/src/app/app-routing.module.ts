import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { ExamplesComponent } from './examples/examples.component';
import { EditorComponent } from './editor/editor.component';
// import { AppComponent } from './app.component';

const routes: Routes = [
    { path: 'home', component: HomeComponent },
    { path: 'project', component: EditorComponent},
    { path: 'examples', component: ExamplesComponent },
    { path: '', redirectTo: '/home', pathMatch: 'full' }, // Redirige al primer componente por defecto
  ];

  @NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
  })
  export class AppRoutingModule { }