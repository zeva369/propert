import { Component, ContentChild, EventEmitter, OnInit, Output } from '@angular/core';
import { WorkFlow } from '../workflow';
import { ProjectService } from '../project.service';
import { Task } from '../task';
import { PertchartComponent } from '../pertchart/pertchart.component';
// import { TaskEditorComponent } from '../task-editor';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  //@Output() tasksUpdated = new EventEmitter<WorkFlow | null>();
  public workflow : WorkFlow |  null = null;
  public error = '';
  
  //@ContentChild(PertchartComponent) chart!: PertchartComponent;
  
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

  public tasks :Task[] = [ 
    { id: "A", 
      description: "Una tarea",
      length: 3
    },
    { id: "B", 
      description: "Una tarea",
      length: 2,
      predecessors: ["A"]
    },
    { id: "C", 
      description: "Una tarea",
      length: 4,
      predecessors: ["B","M"]
    },
    { id: "D", 
      description: "Una tarea",
      length: 10,
      predecessors: ["B","M"]
    },
    { id: "E", 
      description: "Una tarea",
      length: 1,
      predecessors: ["C"]
    },
    { id: "K", 
      description: "kk",
      length: 6,
      predecessors: ["D"]
    },
    { id: "M", 
      description: "mmmm..",
      length: 6,
      predecessors: ["A"]
    } 
  ];

  constructor (private projectService: ProjectService) { }
  
  ngOnInit(): void {
    this.updateWorkflow();
  }

  // Método para actualizar el workflow cuando las tareas cambian
  updateTasks(tasks: Task[]): void {
    this.tasks = tasks;
    this.updateWorkflow();
  }

  updateWorkflow() {
    // if (!this.workflow is null) this.chart.
    this.projectService.getWorkFlow(this.tasks)
    .subscribe({ 
      next : pWorkflow => this.workflow = new WorkFlow(pWorkflow?.nodes, pWorkflow?.edges),
      error : (e) => { 
        console.log(e.message)
        this.error = e.message;
        this.workflow = null
      }
    })
  }

}
