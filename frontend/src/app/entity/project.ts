import { Task } from "./task";

export class Project {
    public id  = 0;
    public name = '';
    public description = '';
    public tasks: Task[] = [];

    constructor(id: number, name: string, description: string, tasks: Task[]) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.tasks = tasks;
    }
}