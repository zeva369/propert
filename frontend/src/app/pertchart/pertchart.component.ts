
import { DataSet } from "vis-data";
import { Network, Options } from "vis-network";
import { Workflow } from "../entity/workflow";
import { Component, effect, Input, input, Signal, signal, WritableSignal } from "@angular/core";
import { NodePosition } from "./node.position";
import { Task } from "../entity/task";

@Component({
  selector: 'app-pertchart',
  templateUrl: './pertchart.component.html',
  styleUrls: ['./pertchart.component.css']
})
export class PertchartComponent {
  @Input({ required: true }) workflow = signal<Workflow | undefined>(undefined);
  @Input() error = signal<string>('');
  @Input({ required: true }) selected = signal<Task | undefined>(undefined);

  private readonly _keepPositions: WritableSignal<boolean> = signal(false);

  private network: Network | undefined = undefined;
  private positions: Record<string, NodePosition> = {};
  public scale = 1.0;

  constructor() {
    // Reacciona cuando cambia el elemento seleccionado
    // seleccionando el Edge correspondiente en el gráfico
    effect(() => {
      const selectedTask = this.selected();  // Track selected
      if (this.network && selectedTask) {
        this.network.selectEdges([selectedTask.id]);
      }
    });

    // Reacciona cuando cambia el workflow o el error
    effect(() => {
      const wf = this.workflow();
      const error = this.error();
      if (wf || error) {
        if (this.keepPositions) this.savePositions();
        this.refresh();
      }
    });
  }

  @Input()
  set keepPositions(value: boolean) {
    this._keepPositions.set(value);
  }

  get keepPositions(): boolean {
    return this._keepPositions();
  }

  private refresh() {
    const networkDiv = document.getElementById("network");
    if (!networkDiv) throw new Error("No se encontró el contenedor del gráfico");

    const wf = this.workflow();
    if (!wf) {
      console.log("Algo va mal con el workflow");
      return;
    }

    const keepPos = this.keepPositions;
    const nodes = Object.values(wf.nodes).map(node => ({
      id: node.id,
      ...(node.label === 'I' || node.label === 'F' ? { label: node.label.trim() } : {}),
      ...(keepPos && this.positions[node.id] ? { x: this.positions[node.id].x, y: this.positions[node.id].y } : {})
    }));

    const edges = Object.values(wf.edges).map(edge => ({
      id: edge.id,
      label: edge.label.trim(),
      from: edge.from,
      to: edge.to,
      width: edge.critical ? 2 : 1,
      color: edge.critical ? '#4f0c75' : '#6E6E6E',
    }));

    const graphNodes = new DataSet(nodes);
    const graphEdges = new DataSet(edges);

    const networkData = { nodes: graphNodes, edges: graphEdges };
    const networkOptions: Options = {
      interaction: {
        hover: true,
        selectConnectedEdges: false,
      },
      edges: {
        font: {
          color: "#EEEEEE",
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
      },
      nodes: {
        font: {
          color: "#EEEEEE",
          align: "center",
          face: "Lucida Console",
        },
        borderWidth: 2,
        shape: "circle",
        shadow: false,
        physics: false,
        size: 25,
        scaling: {
          min: 25,
          max: 25,
          label: false
        },
        color: {
          border: "#4f0c75",
          background: "#1a1a1a",
          highlight: {
            border: "#b019d6",
            background: "#3E3E3E"
          },
          hover: {
            border: "#8722d4",
            background: "#292828",
          },
        },
      },
    };

    if (!this.network) {
      this.network = new Network(networkDiv, networkData, networkOptions);
    } else {
      this.scale = this.network.getScale();
      this.network.setOptions(networkOptions);
      this.network.setData(networkData);
      this.network.once("afterDrawing", () => {
        this.network?.moveTo({
          scale: this.scale,
          animation: {
            duration: 500,
            easingFunction: 'easeInOutQuad'
          }
        });
      });
    }

    this.network.on("click", (evt) => console.log("click: ", evt));
    this.network.on("selectNode", () => this.savePositions());
    this.network.on("deselectNode", (evt) => console.log("deselectNode: ", evt));
    this.network.on("selectEdge", (evt) => console.log("selectEdge: ", evt));
    this.network.on("zoom", (params) => this.saveScale(params));
  }

  private savePositions(): void {
    if (this.network) {
      this.positions = this.network.getPositions();
    }
  }

  private saveScale(params: any): void {
    if (this.network) {
      this.scale = params.scale;
    }
  }

  public onLockButtonClick(): void {
    this._keepPositions.set(!this._keepPositions());
  }

  public onZoomInClick(): void {
    this.scale += 0.15;
    this.network?.moveTo({
      scale: this.scale,
      animation: {
        duration: 500,
        easingFunction: 'easeInOutQuad'
      }
    });
  }

  public onZoomOutClick(): void {
    this.scale -= 0.15;
    this.network?.moveTo({
      scale: this.scale,
      animation: {
        duration: 500,
        easingFunction: 'easeInOutQuad'
      }
    });
  }
}
