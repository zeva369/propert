import { Component, effect, signal, inject, Injector, ChangeDetectionStrategy} from '@angular/core';
//Services
import { UserService } from '../service/user.service';
import { ProjectService } from '../service/project.service';
import { WorkflowService } from '../service/workflow.service';
//Model
import { Task } from '../entity/task';
import { Workflow } from '../entity/workflow';
import { Project } from '../entity/project';
import { User } from '../entity/user';

@Component({
  selector: 'app-editor',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './editor.component.html',  
  styleUrls: ['./editor.component.css'],
})
export class EditorComponent  {
  // Declare & inject services
  private readonly userService = inject(UserService);
  private readonly projectService = inject(ProjectService);
  private readonly workflowService = inject(WorkflowService);
  private readonly injector = inject(Injector);

  // Internal signals
  currentUser = signal<User | undefined>(undefined);
  projects = signal<Project[]>([]);
  currentProject = signal<Project | undefined>(undefined);
  tasks = signal<Task[]>([]);
  workflow = signal<Workflow | undefined>(undefined);
  selectedTask = signal<Task | undefined>(undefined);
  error = signal('');

  constructor() {
    this.initEffects();
    const user = this.userService.getUser();
    this.currentUser.set(user);
  }

  initEffects() {
    // Cuando cambia el usuario, se actualizan los proyectos
    effect(() => {
      const user = this.currentUser();
      if (user) {
        queueMicrotask(() => this.loadProjects(user.id));
      }
    });  

    // Cuando cambia la lista de proyectos, se actualiza el proyecto actual
    effect(() => {
      const projects = this.projects();
      if (projects && projects.length > 0) {
        queueMicrotask(() => this.updateCurrentProject(projects[0]));
      }
    });

    // Cuando cambia el proyecto actual, se actualizan las tareas
    effect(() => {
      const project = this.currentProject();
      if (project) {
        queueMicrotask(() => this.updateTasks(project.tasks));
      } 
    });

    // Cuando cambian las tareas, se actualiza el workflow
    effect(() => {
      const taskList = this.tasks();
      queueMicrotask(() => this.loadWorkflow(taskList));
    });
  }

  /*
  // Cuando cambia el usuario, se actualizan los proyectos
  readonly userChangedEffect = effect(() => {
    const user = this.currentUser();
    if (user) {
      // Usamos microtask para salir del ciclo reactivo
      queueMicrotask(() => this.loadProjects(user.id));
    }
  });  

  // Cuando cambia la lista de proyectos, se actualiza el proyecto actual
  readonly projectsChangedEffect = effect(() => {
    const projects = this.projects();
    if (projects && projects.length > 0) {
      queueMicrotask(() => this.updateCurrentProject(projects[0]));
    }
  });

  // Cuando cambia el proyecto actual, se actualizan las tareas
  readonly currentProjectChangedEffect = effect(() => {
    const project = this.currentProject();
    if (project) {
      queueMicrotask(() => this.updateTasks(project.tasks));
    } 
  });

  // Cuando cambian las tareas, se actualiza el workflow
  readonly tasksChangedEffect = effect(() => {
    const taskList = this.tasks();
    if (taskList.length > 0) {
      queueMicrotask(() => this.loadWorkflow(taskList));
    }
  });

  */

  // Método separado para manejar la lógica asíncrona
  private loadProjects(userId: string): void {
    console.log('Cargando proyectos para el usuario:', userId);
    this.projectService.getProjects(userId).subscribe({
      next: (projects) => this.projects.set(projects),        
      error: (e) => this.handleProjectError(e.message),
    });
  }
  
  updateCurrentProject(project: Project | undefined) {
    console.log('Actualizando proyecto actual:', project);
    this.currentProject.set(project);
  }

  // Método separado para manejar la lógica asíncrona
  private loadWorkflow(taskList: Task[]): void {
    console.log('Cargando workflow para la lista de tareas:', taskList);
    this.workflowService.getWorkFlow(taskList).subscribe({
      next: (wf) => this.workflow.set(new Workflow(wf.nodes, wf.edges)),
      error: (e) => this.handleWorkflowError(e.message),
    });
  }    

  private handleProjectError(message: string): void {
    this.error.set(message);
    this.projects.set([]);
  }

  private handleWorkflowError(message: string): void {
    this.error.set(message);
    this.workflow.set(undefined);
  }

 

  updateTasks(tasks: Task[]) {
    if (tasks && tasks.length > 0) {
      this.tasks.set(tasks);
    } else {
      this.tasks.set([]);
    }
  }

  selectTask(task: Task) {
    this.selectedTask.set({ ...task });
  }
}
