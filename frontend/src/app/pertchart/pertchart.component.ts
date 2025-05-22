
import { DataSet } from "vis-data";
import { Network, Options } from "vis-network";
import { Workflow } from "../entity/workflow";
import { ChangeDetectionStrategy, Component, effect, Input, input, OnInit, Signal, signal, WritableSignal } from "@angular/core";
import { NodePosition } from "./node.position";
import { Task } from "../entity/task";

@Component({
  selector: 'app-pertchart',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './pertchart.component.html',  
  styleUrls: ['./pertchart.component.css']
})
export class PertchartComponent { //implements OnInit {

  // Private signals to manage the workflow, selected task, and error messages
  private readonly _workflow = signal<Workflow | undefined>(undefined);
  private readonly _selectedTask = signal<Task | undefined>(undefined);
  private readonly _error = signal<string>('');
  private readonly _keepPositions: WritableSignal<boolean> = signal(false);

  private network: Network | undefined = undefined;
  private positions: Record<string, NodePosition> = {};
  public scale = 1.0;

  // Reacciona cuando cambia el elemento seleccionado
  // seleccionando el Edge correspondiente en el gráfico
  readonly taskSelectedEffect = effect(() => {
    const selectedTask = this._selectedTask();  // Track selected
    if (this.network && selectedTask) {
      this.network.selectEdges([selectedTask.label]);
    }
  });

  // Reacciona cuando cambia el workflow o el error
  readonly workflowChangedEffect = effect(() => {
    const wf = this._workflow();
    const error = this._error();
    if (wf || error) {
      if (this.keepPositions) this.savePositions();
      queueMicrotask(() => this.refresh());
    }
  });

  @Input()
  set keepPositions(value: boolean) {
    this._keepPositions.set(value);
  }

  get keepPositions(): boolean {
    return this._keepPositions();
  }

  @Input()
  set selectedTask(value: Task | undefined) {
    this._selectedTask.set(value);
  }

  @Input()
  set workflow(value: Workflow | undefined) {
    this._workflow.set(value);
  }

  @Input()
  set error(value: string) {
    this._error.set(value);
  }

  get error (): string {
    return this._error(); 
  }
  
  private refresh() {
    const networkDiv = document.getElementById("network");
    if (!networkDiv) throw new Error("No se encontró el contenedor del gráfico");

    const wf = this._workflow();
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
