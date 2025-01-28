export class Edge {
    id! : number;
    label! : string;
    critical! :boolean;
    from! :number;
    to! :number;

    constructor(data?: Partial<Edge>) {
        Object.assign(this, data);
    }
}