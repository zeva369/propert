export class Node {
    id! :number;
    label! :string;
    start! :number;
    end! :number;
    critical! :boolean;
    initial! :boolean;
    final! :boolean;
    x?:number;
    y?:number;

    constructor(data?: Partial<Node>) {
        Object.assign(this, data);
    }
}