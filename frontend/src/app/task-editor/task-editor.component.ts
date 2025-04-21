import { ChangeDetectionStrategy, Component, effect, EventEmitter, input, Input, Output, signal, WritableSignal } from '@angular/core';
import { Task } from '../entity/task';

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
  
  get taskId(): string {
    return this.selectedTask.id;
  }

  set taskId(value: string) {
    if (this.selectedTask.id == '') {
      this.selectedTask.id = value.trim();
    } else {
      const ts = this._tasks().filter(t => t.id == value);
      if (ts.length > 0) {
        this.selectTask(ts[0]);
      } else {
        this.clear();
        this.selectedTask.id = value.trim();
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
    return this.selectedTask?.predecessors.join(', ') || '';
  }

  set taskPredecessors(value: string) {
    if (value !== "") {
      this.selectedTask.predecessors = value.split(',')
        .map(s => s.trim())
        .filter(s => s.length > 0);
    }
  }

  publishTasksUpdated(): void {
    this.tasksUpdated.emit(this._tasks());
  }

  save(): void {
    const tasks = this._tasks();

    // Check if the task is new
    const taskIndex = tasks.findIndex(t => t.id === this.selectedTask.id);
    if (taskIndex === -1) {
      // If the task is new, add it to the list of tasks
      //tasks.push(this.selectedTask);
      this._tasks.update(tasks => [...tasks, this.selectedTask]);
    } else {
      // Else update the existing task
      //tasks[taskIndex] = this.selectedTask;
      this._tasks.update(tasks =>
        tasks.map(task => task.id === this.selectedTask.id ? this.selectedTask : task)
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

  removeTask(taskId: string): void {
    this._tasks.set(this._tasks().filter(t => t.id != taskId));
    this.publishTasksUpdated(); // Emitir los cambios después de eliminar una tarea
  }

  editTask(updatedTask: Task): void {
    this._tasks.update(tasks =>
      tasks.map(task => task.id === updatedTask.id ? updatedTask : task)
    );
    this.publishTasksUpdated(); // Emitir los cambios después de editar una tarea
  }

}
