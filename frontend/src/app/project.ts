import { Task } from "./task";

export class Project {
    public id  = 0;
    public name = '';
    public description = '';
    public tasks: Task[] = [];

}