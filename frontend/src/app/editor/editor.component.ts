import { runInInjectionContext, Component, effect, signal, computed, inject, Injector} from '@angular/core';
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
  templateUrl: './editor.component.html',
  styleUrls: ['./editor.component.css'],
})
export class EditorComponent  {
  private readonly userService = inject(UserService);
  private readonly projectService = inject(ProjectService);
  private readonly workflowService = inject(WorkflowService);

  currentUser = signal<User | null>(null);
  projects = signal<Project[]>([]);
  currentProject = signal<Project | null>(null);
  tasks = signal<Task[]>([]);
  workflow = signal<Workflow | undefined>(undefined);
  selectedTask = signal<Task | undefined>(undefined);
  error = signal('');

  constructor() {
    this.init();
    this.initUser();
  }

  private initUser() {
    //TODO: Que el usuario me lo pase el componente padre
    const user = this.userService.getUser();
    this.currentUser.set(user);
  }

  private init() {
    // Cuando cambia el usuario, se actualizan los proyectos
    effect(() => {
      const user = this.currentUser();
      if (user) {
        this.userService.setUser(user);
        this.loadProjects(user.id);
      }
    });    

    // Cuando cambia la lista de proyectos, se actualiza el proyecto actual
    effect(() => {
      const projects = this.projects();
      if (projects.length > 0) {
        this.updateCurrentProject(projects[0]);
      }
    });

    // Cuando cambia el proyecto actual, se actualizan las tareas
    effect(() => {
      const project = this.currentProject();
      if (project) {
        this.updateTasks(project.tasks);
      } else {
        this.tasks.set([]);
      }
    });

    // Cuando cambian las tareas, se actualiza el workflow
    effect(() => {
      const taskList = this.tasks();
      if (taskList.length > 0) {
        this.loadWorkflow(taskList);
      }
    });
  }

  // Método separado para manejar la lógica asíncrona
  private loadProjects(userId: string): void {
    this.projectService.getProjects(userId).subscribe({
      next: (projects) => this.projects.set(projects),
      error: (e) => this.handleProjectError(e.message),
    });
  }

  // Método separado para manejar la lógica asíncrona
  private loadWorkflow(taskList: Task[]): void {
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

  updateCurrentProject(project: Project) {
    this.currentProject.set(project);
  }

  updateTasks(tasks: Task[]) {
    this.tasks.set(tasks);
  }

  onTaskSelected(task: Task) {
    this.selectedTask.set(task);//{ ...task });
  }
}
