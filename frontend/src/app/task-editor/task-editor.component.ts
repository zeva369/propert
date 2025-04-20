import { Component, effect, EventEmitter, Input, input, Output, signal, Signal } from '@angular/core';
import { Task } from '../entity/task';

@Component({
  selector: 'app-task-editor',
  templateUrl: './task-editor.component.html',
  styleUrls: ['./task-editor.component.css']
})
export class TaskEditorComponent {
  @Output() tasksUpdated = new EventEmitter<Task[]>();
  @Output() taskSelected = new EventEmitter<Task>();
  @Input({ required: true }) tasks = signal<Task[]>([]);
  
  selectedTask: Task = new Task();

  constructor() {
    this.init();
  }

  init() {
    effect(() => {
      if (this.tasks()) {
          this.publishTasksUpdated(); // Actualiza las tareas
      }
    });
  }

  get taskId(): string {
    return this.selectedTask.id;
  }

  set taskId(value: string) {
    if (this.selectedTask.id == '') {
      this.selectedTask.id = value.trim();
    } else {
      const ts = this.tasks().filter(t => t.id == value);
      if (ts.length > 0) {
        this.selectTask(ts[0]);
      } else {
        this.clear();
        this.selectedTask.id = value.trim();
      }   
    }
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
    this.tasksUpdated.emit(this.tasks());
  }

  save(): void {
    const tasks = this.tasks();
    // Check if the task is new
    const taskIndex = tasks.findIndex(t => t.id === this.selectedTask.id);
    if (taskIndex === -1) {
      // If the task is new, add it to the list of tasks
      //tasks.push(this.selectedTask);
      this.tasks.update(tasks => [...tasks, this.selectedTask]);
    } else {
      // Else update the existing task
      tasks[taskIndex] = this.selectedTask;
    }
    //this.publishTasksUpdated(); // Emitir los cambios después de añadir una nueva tarea
    this.clear();
  }

  public selectTask(task: Task): void {
    this.selectedTask = { ...task};
    this.taskSelected.emit(task);
  }

  clear(): void {
    this.selectedTask = new Task();
  }

  removeTask(taskId: string): void {
    this.tasks.set(this.tasks().filter(t => t.id != taskId));
    this.publishTasksUpdated(); // Emitir los cambios después de eliminar una tarea
  }

  editTask(task: Task): void {
    const tasks = this.tasks();
    if (tasks && tasks.length > 0) {
      const index = tasks.indexOf(task, 0);

      if (index > -1 &&
        tasks.length >= index) {
        tasks[index] = task;
      }
    }
    this.publishTasksUpdated(); // Emitir los cambios después de editar una tarea
  }

}
