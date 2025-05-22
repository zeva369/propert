import { Predecessor } from "./predecessor";

export class Task {
    public id  = '';
    public label = '';
    public description = '';
    public length = 0;
    public predecessors: Predecessor[] = [];

}