import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { Task } from '../entity/task';

@Component({
  selector: 'app-task-editor',
  templateUrl: './task-editor.component.html',
  styleUrls: ['./task-editor.component.css']
})
export class TaskEditorComponent implements OnChanges {
  @Output() tasksUpdated = new EventEmitter<Task[]>();
  @Output() taskSelected = new EventEmitter<Task>();
  @Input() tasks: Task[] = [];
  selectedTask: Task = new Task();

  // taskId = '';
  // taskDescription = '';
  // taskLength = 0.0;
  // taskPredecessors = '';

  get taskId(): string {
    return this.selectedTask.id;
  }

  set taskId(value: string) {
    if (this.selectedTask.id == '') {
      this.selectedTask.id = value.trim();
    } else {
      let ts = this.tasks.filter(t => t.id == value);
      if (ts.length > 0) {
        this.selectTask(ts[0]);
      } else {
        this.clear();
        this.selectedTask.id = value.trim();
      }   
    }
    // this.taskId = value;
  }

  setTaskDescription(value: string) {
    this.selectedTask.description = value;
    // this.taskDescription = value;
  }

  setTaskLength(value: number) {
    this.selectedTask.length = value;
    // this.taskLength = value;
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
    // this.taskPredecessors = value;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['tasks']) {
      this.updateTasks();
    }
  }

  updateTasks(): void {
    this.tasksUpdated.emit(this.tasks);
  }

  save(): void {
    // Check if the task is new
    const taskIndex = this.tasks.findIndex(t => t.id === this.selectedTask.id);
    if (taskIndex === -1) {
      // If the task is new, add it to the list of tasks
      this.tasks.push(this.selectedTask);
    } else {
      // Else update the existing task
      this.tasks[taskIndex] = this.selectedTask;
    }
    this.updateTasks(); // Emitir los cambios después de añadir una nueva tarea
    this.clear();
  }

  public selectTask(task: Task): void {
    this.selectedTask = { ...task};
    this.taskSelected.emit(task);
    // this.taskId = this.selectedTask.id;
    // this.taskDescription = this.selectedTask.description;
    // this.taskLength = this.selectedTask.length;
    // this.taskPredecessors = this.selectedTask.predecessors.join(",");
  }

  clear(): void {
    this.selectedTask = new Task();
    // this.taskId = '';
    // this.taskDescription = '';
    // this.taskLength = 0.0;
    // this.taskPredecessors = '';
  }

  // addTask(newTask: Task): void {
  //   this.tasks.push(newTask);
  //   this.updateTasks(); // Emitir los cambios después de añadir una nueva tarea
  // }

  removeTask(taskId: string): void {
    this.tasks = this.tasks.filter(t => t.id != taskId);
    this.updateTasks(); // Emitir los cambios después de eliminar una tarea
  }

  editTask(task: Task): void {
    if (this.tasks) {
      const index = this.tasks.indexOf(task, 0);

      if (index > -1 &&
        this.tasks.length >= index) {
        this.tasks[index] = task;
      }
    }
    this.updateTasks(); // Emitir los cambios después de editar una tarea
  }

}
