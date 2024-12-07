import {DataSet} from "vis-data"
import { Network, Options } from "vis-network";
import { WorkFlow } from "../workflow";
import { Component, Input, OnChanges,  Output,  SimpleChanges } from "@angular/core";
import { NodePosition } from "./node.position";

@Component({
    selector: 'app-pertchart',
    templateUrl: './pertchart.component.html',
    styleUrls: ['./pertchart.component.css']
  })
export class PertchartComponent implements OnChanges{
   @Input() workflow : WorkFlow | undefined = undefined;
   @Input() error = '';

   private network : Network | null = null;
   private positions : Record<string, NodePosition> = {};
   private locked = false;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['workflow'] || changes['error']) {
      if (this.workflow != null && this.locked) {
        this.savePositions();
      }
      this.refresh();
    }
  }

  private refresh() {
    // Obtener el contenedor del gráfico
    const networkDiv = document.getElementById("network");
    if (!networkDiv) {
      throw new Error("No se encontró el contenedor del gráfico");
    }

    if (!this.workflow ) {
      console.log("Algo va mal con el workflow")
      return;
    }

    if (this.positions != null && this.locked) console.log(this.positions);

    const nodes = Object.values(this.workflow?.nodes)?.map(node => ({
      id: node.id,
      label: node.label,
      ...(node.critical  ? {border:'#ff0000'} : {}),
      ...((this.positions != null && this.locked && this.positions[node.id] != null) ? {x:this.positions[node.id].x} : {}),
      ...((this.positions != null && this.locked && this.positions[node.id] != null) ? {y:this.positions[node.id].y} : {})
    }));

    const edges = Object.values(this.workflow?.edges)?.map(edge => ({
      id: edge.id,
      label: edge.label,
      from: edge.from,
      to: edge.to,
      color: edge.critical ? '#FF0000' : '#2D68B9',
    }));

    const graphNodes = new DataSet(nodes);
    const graphEdges = new DataSet(edges);

    // Configurar el gráfico
    const networkData = { nodes: graphNodes, edges: graphEdges };
    const networkOptions: Options = {
      interaction: {
        hover: true,
        selectConnectedEdges: false,
      },
      edges: {
        font: {
          color: "#333",
          strokeWidth: 0.5,
        },
        arrows: {
          to: {
            enabled: true,
            type: "arrow",
            scaleFactor: 0.6,
          },
        },
        /*
        smooth: {
          type: 'curvedCW', // Tipo de curva
          roundness: 0.2,
        }
        */
      },
      nodes: {
        shadow: false,
        physics: false,
        color: {
          background: "#FFFFFF",
          highlight: {
            border: "#2B7CE9",
            background: "#D2E5FF",
          },
          hover: {
            border: "#2B7CE9",
            background: "#D2E5FF",
          },
        },
      },
    };

    // Crear el gráfico
    this.network = new Network(networkDiv, networkData, networkOptions);

    // Configurar eventos
    this.network.on("click", (evt) => console.log("click: ", evt));
    this.network.on("selectNode", () => this.savePositions());
    this.network.on("deselectNode", (evt) => console.log("deselectNode: ", evt));
    this.network.on("selectEdge", (evt) => console.log("selectEdge: ", evt));
  }

  // Método para guardar posiciones (debes implementar la lógica)
  private savePositions(): void {
    if (!this.network) return;

    this.positions = this.network.getPositions();
  }

  //Toggle locked positions state
  public onLockButtonClick(): void{
    this.locked = !this.locked
  }
}
