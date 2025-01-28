import { Component, Input, OnInit } from '@angular/core';
import { Task } from '../entity/task';
import { UserService } from '../service/user.service';
import { ProjectService } from '../service/project.service';
import { WorkflowService } from '../service/workflow.service';
import { Workflow } from '../entity/workflow';
import { Project } from '../entity/project';
import { User } from '../entity/user';

@Component({
  selector: 'app-editor',
  templateUrl: './editor.component.html',
  styleUrls: ['./editor.component.css']
})
export class EditorComponent implements OnInit {
  //@Output() tasksUpdated = new EventEmitter<WorkFlow | null>();
  public workflow: Workflow | undefined = undefined;
  public currentProject : Project | undefined = undefined;
  public error = '';

  currentUser: User | null = null;
  tasks: Task[] = [];
  projects: Project[] = [];
  selectedTask: Task | null = null;

  // private tasks = [ 
  //   { id: "A", 
  //     description: "Una tarea",
  //     length: 2
  //   },
  //   { id: "B", 
  //     description: "Una tarea",
  //     length: 2,
  //     predecessors: ["A","C"]
  //   },
  //   { id: "C", 
  //     description: "Una tarea",
  //     length: 4
  //   },
  //   { id: "D", 
  //     description: "Una tarea",
  //     length: 10,
  //     predecessors: ["A"]
  //   },
  //   { id: "E", 
  //     description: "Una tarea",
  //     length: 1,
  //     predecessors: ["C","F"]
  //   },
  //   { id: "F", 
  //     description: "Una tarea",
  //     length: 12,
  //     predecessors: ["B"]
  //   },
  //   { id: "G", 
  //     description: "Una tarea",
  //     length: 2,
  //     predecessors: ["D"]
  //   },
  //   { id: "H", 
  //     description: "Una tarea",
  //     length: 6
  //   }
  // ];

  // public tasks :Task[] = [ 
  //   { id: "A", 
  //     description: "Una tarea",
  //     length: 3
  //   },
  //   { id: "B", 
  //     description: "Una tarea",
  //     length: 2
  //   },
  //   { id: "C", 
  //     description: "Una tarea",
  //     length: 4,
  //     predecessors: ["A", "B"]
  //   },
  //   { id: "D", 
  //     description: "Una tarea",
  //     length: 10,
  //     predecessors: ["C"]
  //   },
  //   { id: "E", 
  //     description: "Una tarea",
  //     length: 1,
  //     predecessors: ["A"]
  //   },
  //   { id: "K", 
  //     description: "kk",
  //     length: 6,
  //     predecessors: ["C"]
  //   } 
  // ];

  // public tasks :Task[] = [ 
  //   { id: "A", 
  //     description: "Una tarea",
  //     length: 3
  //   },
  //   { id: "B", 
  //     description: "Una tarea",
  //     length: 2,
  //     predecessors: ["A"]
  //   },
  //   { id: "C", 
  //     description: "Una tarea",
  //     length: 4,
  //     predecessors: ["B","M"]
  //   },
  //   { id: "D", 
  //     description: "Una tarea",
  //     length: 10,
  //     predecessors: ["B","M"]
  //   },
  //   { id: "E", 
  //     description: "Una tarea",
  //     length: 1,
  //     predecessors: ["C"]
  //   },
  //   { id: "K", 
  //     description: "kk",
  //     length: 6,
  //     predecessors: ["D"]
  //   },
  //   { id: "M", 
  //     description: "mmmm..",
  //     length: 6,
  //     predecessors: ["A"]
  //   } 
  // ];


  constructor(private readonly userService: UserService,
              private readonly projectService: ProjectService,
              private readonly workflowService: WorkflowService) { }

  ngOnInit(): void {
    //Obtengo el usuario
    this.currentUser = this.userService.getUser();
    //Actualizo la lista de proyectos y selecciono el primero
    if (this.currentUser) {
      this.projectService.getProjects(this.currentUser.id)
      .subscribe({ 
        next : pProjects => {
            this.projects = pProjects;
            if (this.projects.length>=1) this.updateCurrentProject(this.projects[0]);
        },
        error : (e) => { 
          this.error = e.message;
          this.projects = []
        }
      })
    } else console.log("No user logged in");  
  }

  //Consumo el evento currentProjectChanged del componente ProjectSelector
  updateCurrentProject(currentProject: Project): void {
    this.currentProject = currentProject;
    this.updateTasks(this.currentProject.tasks);
  }

    // Consumo el evento tasksUpdated del componente TaskEditor
  // actualizando el workflow cuando las tareas cambian
  updateTasks(tasks: Task[]): void {
    this.tasks = tasks;
    this.updateWorkflow();
  }
  
  updateWorkflow() {
    this.workflowService.getWorkFlow(this.tasks)
    .subscribe({ 
      next : pWorkflow => this.workflow = new Workflow(pWorkflow.nodes, pWorkflow.edges),
      error : (e) => { 
        console.log(e.message)
        this.error = e.message;
        this.workflow = undefined
      }
    })
  }

  onTaskSelected(task: Task): void {
    this.selectedTask = { ...task };
  }

}
