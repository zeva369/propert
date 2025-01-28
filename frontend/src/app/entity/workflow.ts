// import { Node } from 'vis-network';
// import { Edge } from 'vis-network';
import { Node } from './node';
import { Edge } from './edge';

export class Workflow { 
    public nodes: Record<number, Node> = {};
    public edges: Record<string, Edge> = {};
    
    constructor(nds : Record<number, Node>, eds : Record<string, Edge>) {
          this.nodes = nds;
          this.edges = eds;
    }
}