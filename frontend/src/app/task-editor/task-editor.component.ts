import { ChangeDetectionStrategy, Component, effect, EventEmitter, input, Input, Output, signal, WritableSignal } from '@angular/core';
import { Task } from '../entity/task';
import { Predecessor } from '../entity/predecessor';

@Component({
  selector: 'app-task-editor',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './task-editor.component.html',
  styleUrls: ['./task-editor.component.css']
})
export class TaskEditorComponent {
  @Output() tasksUpdated = new EventEmitter<Task[]>();
  @Output() taskSelected = new EventEmitter<Task>();
  private readonly _tasks: WritableSignal<Task[]> = signal<Task[]>([]);
  //tasks = input<Task[]>([]);

  selectedTask: Task = new Task();

  readonly tasksUpdatedEffect = effect(() => {
    const tasks = this._tasks();
    if (tasks && tasks.length > 0) {
        this.publishTasksUpdated(); // Actualiza las tareas
    }
  });
  
  get taskLabel(): string {
    return this.selectedTask.label;
  }

  set taskLabel(value: string) {
    if (this.selectedTask.label == '') {
      this.selectedTask.label = value.trim();
    } else {
      const ts = this._tasks().filter(t => t.label == value);
      if (ts.length > 0) {
        this.selectTask(ts[0]);
      } else {
        this.clear();
        this.selectedTask.label = value.trim();
      }   
    }
  }

  @Input()
  set tasks(value: Task[]) {
    this._tasks.set(value);
  }

  get tasks(): Task[] {
    return this._tasks();
  }

  setTaskDescription(value: string) {
    this.selectedTask.description = value;
  }

  setTaskLength(value: number) {
    this.selectedTask.length = value;
  }

  get taskPredecessors() : string{
    return this.selectedTask.predecessors
    .map(pre => pre.label)
    .filter(label => !!label)
    .join(', ');
  }

  set taskPredecessors(value: string) {
    if (value !== "") {
      const labels = value.split(',')
        .map(s => s.trim())
        .filter(s => s.length > 0);

      // Busca los objetos Task correspondientes a cada label
      this.selectedTask.predecessors = labels
        .map(label => this._tasks().find(t => t.label === label))
        .filter((task): task is Task => !!task) // Filtra los que no existen
        .map(task => ({ id: task.id, label: task.label })); // Guarda solo id y label, o el objeto completo si lo prefieres
    } else {
      this.selectedTask.predecessors = [];
    }
  }

  // Método para obtener las etiquetas de los predecesores de una tarea específica, no la seleccionada
  getPredecessorLabels(task: Task): string {
    return (task.predecessors || [])
      .map((p: Predecessor) => p.label)
      .join(', ');
 }

  publishTasksUpdated(): void {
    this.tasksUpdated.emit(this._tasks());
  }
 
  save(): void {
    const tasks = this._tasks();

    // Check if the task is new
    const taskIndex = tasks.findIndex(t => t.label === this.selectedTask.label);
    if (taskIndex === -1) {
      // If the task is new, add it to the list of tasks
      //tasks.push(this.selectedTask);
      //Set the new task id with a new UUID randomly generated
      this.selectedTask.id = crypto.randomUUID();
      this._tasks.update(tasks => [...tasks, this.selectedTask]);
    } else {
      // Else update the existing task
      //tasks[taskIndex] = this.selectedTask;
      this._tasks.update(tasks =>
        tasks.map(task => task.label === this.selectedTask.label ? this.selectedTask : task)
      );
    }
    //this.publishTasksUpdated(); // Emitir los cambios después de añadir una nueva tarea
    this.clear();
  }

  public selectTask(task: Task): void {
    //const t = { ...task};
    this.selectedTask = task;
    this.taskSelected.emit(task);
  }

  clear(): void {
    this.selectedTask = new Task();
  }

  removeTask(taskLabel: string): void {
    this._tasks.set(this._tasks().filter(t => t.label != taskLabel));
    this.publishTasksUpdated(); // Emitir los cambios después de eliminar una tarea
  }

  editTask(updatedTask: Task): void {
    this._tasks.update(tasks =>
      tasks.map(task => task.label === updatedTask.label ? updatedTask : task)
    );
    this.publishTasksUpdated(); // Emitir los cambios después de editar una tarea
  }

}
