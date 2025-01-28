import { DataSet } from "vis-data"
import { Network, Options } from "vis-network";
import { Workflow } from "../entity/workflow";
import { Component, Input, OnChanges, Output, SimpleChanges } from "@angular/core";
import { NodePosition } from "./node.position";
import { Task } from "../entity/task";

@Component({
  selector: 'app-pertchart',
  templateUrl: './pertchart.component.html',
  styleUrls: ['./pertchart.component.css']
})
export class PertchartComponent implements OnChanges {
  @Input() workflow: Workflow | undefined = undefined;
  @Input() error = '';
  @Input() selected: Task | null = null;

  private network: Network | null = null;
  private positions: Record<string, NodePosition> = {};
  private locked = false;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['selected']) {
      if (this.network && this.selected) {
        this.network.selectEdges([this.selected.id]);
      }
    }

    if (changes['workflow'] || changes['error']) {
      if (this.workflow != null && this.locked) {
        console.log("Pasa por aqui");
        this.savePositions();
      }
      this.refresh();
    }    
  }

  private refresh() {
    this.error = '';
    // Obtener el contenedor del gráfico
    const networkDiv = document.getElementById("network");
    if (!networkDiv) {
      throw new Error("No se encontró el contenedor del gráfico");
    }

    if (!this.workflow) {
      console.log("Algo va mal con el workflow")
      return;
    }

    if (this.positions != null && this.locked) console.log(this.positions);

    const nodes = Object.values(this.workflow?.nodes)?.map(node => ({
        id: node.id,
        label: node.label,
        //...(node.label == 'I' || node.label == 'F' ? {label: node.label}: {}),
        ...(node.critical ? { border: '#ff0000' } : {}),
        ...((this.positions != null && this.locked && this.positions[node.id] != null) ? { x: this.positions[node.id].x } : {}),
        ...((this.positions != null && this.locked && this.positions[node.id] != null) ? { y: this.positions[node.id].y } : {})
      }));

    const edges = Object.values(this.workflow?.edges)?.map(edge => ({
        id: edge.id,
        label: edge.label,
        from: edge.from,
        to: edge.to,
        color: edge.critical ? '#69211e' : '#3E3E3E',
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
          color: "#EEEEEE",//"#333",
          strokeWidth: 0,
          size: 14,
          face: "Lucida Console",
          align: "top"
        },
        labelHighlightBold: true,
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
        font: {
          color: "#EEEEEE"
        },
        borderWidth: 2,
        shape: "circle",
        shadow: false,
        physics: false,
        size: 25,
        scaling: {
          min: 25, // Tamaño mínimo del nodo
          max: 25, // Tamaño máximo del nodo
          label: {
            enabled: false // Deshabilita el escalado de la etiqueta
          }
        },
        color: {
          border: "#b019d6",
          background: "#1a1a1a",//"#FFFFFF",
          highlight: {
            border: "#b019d6",
            background: "#3E3E3E"//"#D2E5FF",
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
  public onLockButtonClick(): void {
    this.locked = !this.locked
  }
}
