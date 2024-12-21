import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { Task } from '../task';

@Component({
  selector: 'app-task-editor',
  templateUrl: './task-editor.component.html',
  styleUrls: ['./task-editor.component.css']
})
export class TaskEditorComponent implements OnChanges{
  @Output() tasksUpdated = new EventEmitter<Task[]>();
  @Input() tasks : Task[] = [];

  taskId = '';
  taskDescription = '';
  taskLength = 0.0;
  taskPredecessors = '';

  setTaskId(value: string) {
    this.taskId = value; 
  }

  setTaskDescription(value: string) {
    this.taskDescription = value; 
  }

  setTaskLength(value: number) {
    this.taskLength = value; 
  }

  setTaskPredecessors(value: string) {
    this.taskPredecessors = value; 
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['tasks']) {
      this.updateTasks();
    }
  }
  
  updateTasks(): void {
    this.tasksUpdated.emit(this.tasks);
  }

  addTask(): void {
    const newTask = new Task();
    newTask.id = this.taskId;
    newTask.description = this.taskDescription;
    newTask.length = this.taskLength;
    newTask.predecessors = this.taskPredecessors.split(",");

    this.tasks.push(newTask);
    this.updateTasks(); // Emitir los cambios después de añadir una nueva tarea
    this.clear();
  }

  clear(): void {
    this.taskId = '';
    this.taskDescription = '';
    this.taskLength = 0.0;
    this.taskPredecessors = '';
  }

  // addTask(newTask: Task): void {
  //   this.tasks.push(newTask);
  //   this.updateTasks(); // Emitir los cambios después de añadir una nueva tarea
  // }

  removeTask(taskId: string): void {
    const task = this.tasks.filter( t => t.id == taskId)[0];
    const index =  this.tasks.indexOf(task, 0);

    if (index > -1) {
      this.tasks.splice(index, 1);
    }
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
