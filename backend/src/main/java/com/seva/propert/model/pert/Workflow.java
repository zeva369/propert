package com.seva.propert.model.pert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.seva.propert.model.entity.Project;
import com.seva.propert.model.entity.Task;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Workflow {
    @JsonIgnore
    private Project project = null;

    @JsonIgnore
    private Map<String, Task> tasks = null; // Complete clone of tasks
    private Map<Long, Node> nodes = new HashMap<>();
    private Map<String, Edge> edges = new HashMap<>();

    @JsonIgnore
    private Long nodeCounter = 1L;
    @JsonIgnore
    private Long dummyTaskCounter = 1L;
    @JsonIgnore
    private Node firstNode = null;
    @JsonIgnore
    private Node lastNode = null;

    public Workflow(Project project, List<Task> tasks) {
        this.project = project;
        this.tasks = tasks.stream()
                .collect(Collectors.toMap(Task::getId, task -> task.clone()));
        firstNode = new Node(nodeCounter++, "I");
        lastNode = new Node(nodeCounter++, "F");
        nodes.put(firstNode.getId(), firstNode);
        nodes.put(lastNode.getId(), lastNode);
        initialize();
    }

    private void initialize() {
        if (!hasLoop()) {
            calcNextTasks();
            createDummyTasks();
            nestTasks();
            nestNodes();
            calcNodesTimes();
            calcTasksTimes();
            calcCriticPath();
        }
    }

    // PERT specific methods

    // Recursive function: Evaluates if a given task has any loop in their
    // dependencies
    private Boolean hasLoop(Task task, Set<String> unvisited, Set<String> inProcess) {
        if (unvisited.contains(task.getId())) {
            inProcess.add(task.getId()); // Mientras la pongo en la lista de 'en proceso'
            // Busco si alguna dependencia también está en la lista inProcess,
            // si es asi es que hay un ciclo, devuelvo true;
            if (!task.getPredecessors().isEmpty()) {
                // Recorro la lista de ids
                for (Task depTask : task.getPredecessors()) {
                    if (inProcess.contains(depTask.getId())) {
                        return true;
                    } else {
                        // Recorro de forma recursiva
                        if (hasLoop(depTask, unvisited, inProcess))
                            return true;
                    }
                }
            }
            // Ya puedo quitarla de la lista de in Process
            inProcess.remove(task.getId());
            // Tambien puedo quitarlo de la lista de no visitados
            unvisited.remove(task.getId());
        }
        return false;
    }

    // Evaluate if the entire net has any loop
    public Boolean hasLoop() {
        Set<String> unvisited = new HashSet<>(tasks.keySet());
        Set<String> inProcess = new HashSet<>();

        for (Task t : tasks.values()) {
            if (hasLoop(t, unvisited, inProcess))
                return true;
        }
        return false;
    }

    // Calcula las tareas que dependen de c/u (lo contrario al campo prev)
    private void calcNextTasks() {
        tasks.forEach((t, task) -> {
            tasks.forEach((d, dependency) -> {
                if (!t.equals(d) && !dependency.getPredecessors().isEmpty()) {
                    Map<String, Task> predecessors = dependency.getPredecessors()
                            .stream()
                            .collect(Collectors.toMap(Task::getId, element -> element));
                    if (predecessors.containsKey(task.getId())) {
                        task.getDependencies().add(dependency);
                    }
                }
            });
        });
    }

    private void createDummyTasks() {
        List<Task> tasksToAdd = new ArrayList<>();

        Iterator<Task> ts = tasks.values().iterator();
        while (ts.hasNext()) {
            Task task = ts.next();

            // If there are task wich depends on task
            if (!task.getDependencies().isEmpty()) {

                // When more than one task depends on task
                if (task.getDependencies().size() > 1) {

                    Iterator<Task> nextTasks = task.getDependencies().iterator();
                    while (nextTasks.hasNext()) {
                        Task next = nextTasks.next();
  
                        // review if those dependents tasks depend on more than 1 task
                        if (next.getPredecessors().size() > 1) {
                            // This situation is not possible, it require one dummy task to be created
                            // and put in middle of both tasks (task & next)
                            String dummyTaskId = "D" + dummyTaskCounter++;
                            Task dummyTask = new Task(dummyTaskId,
                                    "Dummy task",
                                    0d,
                                    Arrays.asList(task),
                                    this.project,
                                    true, -1d, -1d, -1d, -1d,
                                    Arrays.asList(next));
                            
                            nextTasks.remove();

                            // task.getDependencies().removeIf(r ->
                            // next.getId().equalsIgnoreCase(r.getId()));
                            //task.getDependencies().add(dummyTask);
                            tasksToAdd.add(dummyTask);

                            Iterator<Task> predecessorsIterator = next.getPredecessors().iterator();
                            while (predecessorsIterator.hasNext()) {
                                Task predecessor = predecessorsIterator.next();
                                if (predecessor.getId().equalsIgnoreCase(task.getId())){
                                    predecessorsIterator.remove();
                                }
                            }
                            // next.getPredecessors().removeIf(r ->
                            // task.getId().equalsIgnoreCase(r.getId()));
                            next.getPredecessors().add(dummyTask);
                        }
                    }
                    task.getDependencies().addAll(tasksToAdd);
                }
            }
        }
        //Agrego las tareas dummies al mapa tasks
        Map<String,Task> toAdd = tasksToAdd.stream()
                        .collect(Collectors.toMap(Task::getId, element -> element));
        tasks.putAll(toAdd);
    }

    private void edgeAddOrigin(Task task, Task prev) {

        Edge edgeTask = edges.get(task.getId());
        Edge edgePrev = edges.get(prev.getId());

        if (edges.containsKey(prev.getId())) {

            if (edgePrev.getTo() != null) {
                edgeTask.setFrom(edgePrev.getTo());
                // log.info(edgePrev.getTo().getId() + " -> " + task.getId());
            } else {

            }

        } else {
            // Creo un nuevo nodo y se lo asigno al campo to
            if (edgeTask.getFrom() == null) {
                Long newNodeId = nodeCounter++;
                Node newNode = new Node(newNodeId, newNodeId.toString());
                nodes.put(newNodeId, newNode); // { id: newNode, label: newNode.toString(), start:0, end:0, next:[],
                                               // prev:[]};
                edgeTask.setFrom(newNode);

                // log.info("Se agrega node " + newNodeId);
                // log.info(newNodeId + " -> " + task.getId());

                // Tambien aprovecho y creo el edge siguiente y le asigno el from
                // Edge newEdge = new Edge(prev.getId(), prev.getId() + "," + prev.getLength(),
                // null, newNode, false);
                // edges.put(prev.getId(), newEdge); // {label:prev + "," + tasks[prev].length,
                // from:null, to:newNode,
                // log.info("Se agrega edge " + newEdge.getId());
                // log.info(newEdge.getId() + " -> " + newNodeId);// dashes: tasks[prev].dummy};
            }
        }
    }

    private void edgeAddDestination(Task task, Task next) {
        Edge edgeTask = edges.get(task.getId());
        Edge edgeNext = edges.get(next.getId());

        if (edges.containsKey(next.getId())) {

            // Si existe una arista para la tarea siguiente y esta tiene definido un nodo
            // 'from' entonces lo tomo como nodo 'to' de esta
            if (edgeNext.getFrom() != null) {
                edgeTask.setTo(edgeNext.getFrom());
                // log.info(task.getId() + " -> " + edgeNext.getFrom().getId());
            } else {
                // // Creo un nuevo nodo y se lo asigno al campo to
                // Long newNodeId = nodeCounter++;
                // Node newNode = new Node(newNodeId, newNodeId.toString());
                // edgeTask.setTo(newNode);
                // edgeNext.setFrom(newNode);
                // log.info(task.getId() + " -> " + newNodeId);
            }

        } else {
            if (edgeTask.getTo() == null) {
                // Creo un nuevo nodo y se lo asigno al campo to
                Long newNodeId = nodeCounter++;
                Node newNode = new Node(newNodeId, newNodeId.toString());
                nodes.put(newNodeId, newNode);
                edgeTask.setTo(newNode);

                // log.info("Se agrega node " + newNodeId);
                // log.info(task.getId() + " -> " + newNodeId);

                // Tambien aprovecho y creo el edge siguiente y le asigno el from
                // Edge newEdge = new Edge(next.getId(), next.getId() + "," + next.getLength(),
                // newNode, null, false);
                // edges.put(next.getId(), newEdge);
                // log.info("Se agrega edge " + newEdge.getId());
                // log.info( newNodeId + " -> " + newEdge.getId());
            }
        }
    }

    private void nestTasks() {
        tasks.forEach((t, task) -> {
            // log.info("task: ".concat(t));
            if (!edges.containsKey(t)) {
                Edge newEdge = new Edge(t, t + "," + task.getLength(), null, null, false);
                edges.put(t, newEdge);
                // log.info("Se agrega edge " + t);
            }

            // PREVIOUS
            if (task.getPredecessors().isEmpty()) {
                edges.get(t).setFrom(firstNode);
                // log.info("Se conecta el edge " + t + " al nodo inicial");
            } else {
                // Para c/u de los elementos en predecessors
                task.getPredecessors().forEach((p) -> edgeAddOrigin(task, p));
            }

            // NEXT
            if (task.getDependencies().isEmpty()) {
                edges.get(t).setTo(lastNode);
                // log.info("Se conecta el edge " + t + " al nodo final");
            } else {
                // Para c/u de los elementos en next
                task.getDependencies().forEach(n -> edgeAddDestination(task, n));
            }
        });
    }

    private void nestNodes() {
        for (Edge edge : edges.values()) {
            edge.getFrom().getNext().put(edge.getId(), edge);
            edge.getTo().getPrevious().put(edge.getId(), edge);
        }
    }

    private Double getMaxLength(Map<String, Edge> eds) {
        Double max = 0d;

        for (Edge e : eds.values()) {
            Double newValue = getTasksEarlyStart(e.getFrom()) + tasks.get(e.getId()).getLength();
            if (newValue > max)
                max = newValue;
        }
        return max;
    }

    private Double getMinLength(Map<String, Edge> eds) {
        Double min = 99999999999d;

        for (Edge e : eds.values()) {
            Double newValue = getTasksLateFinish(e.getTo()) - tasks.get(e.getId()).getLength();
            if (newValue < min)
                min = newValue;
        }
        return min;
    }

    private Double getTasksEarlyStart(Node node) {
        if (node.getPrevious().isEmpty())
            return 0d;
        return getMaxLength(node.getPrevious());
    }

    private Double getTasksLateFinish(Node node) {
        if (node.getNext().isEmpty())
            return node.getStart();
        return getMinLength(node.getNext());
    }

    private void calcNodesTimes() {
        // Calculate each node Early Start
        nodes.values().forEach(n -> {
            n.setStart(getTasksEarlyStart(n));
        });

        nodes.values().forEach(n -> {
            n.setEnd(getTasksLateFinish(n));
        });

    }

    // Comienzo del nodo origen
    private Double getTaskEarlyStart(Task task) {
        Edge edge = edges.get(task.getId());
        return edge.getFrom().getStart();
    }

    // Fin del nodo de destino
    private Double getTaskLateFinish(Task task) {
        Edge edge = edges.get(task.getId());
        return edge.getTo().getEnd();
    }

    private void calcTasksTimes() {

        tasks.values().forEach((t) -> {
            // Calcular early start & early finish
            t.setEarlyStart(getTaskEarlyStart(t));
            t.setEarlyFinish(t.getEarlyStart() + t.getLength());
        });

        tasks.values().forEach((t) -> {
            // Calcular latest finish & latest start
            t.setLateFinish(getTaskLateFinish(t));
            t.setLateStart(t.getLateFinish() - t.getLength());
        });
    }

    private void calcCriticPath() {
        // Tareas del camino crítico
        tasks.values().forEach((t) -> {
            if (t.getEarlyStart().equals(t.getLateStart()) &&
                    t.getEarlyFinish().equals(t.getLateFinish())) {
                edges.get(t.getId()).setCritical(true); // .color = '#FF0000';
            } else
                edges.get(t.getId()).setCritical(false); // .color = '#2D68B9';
        });

        // Nodos del camino crítico
        nodes.entrySet().forEach(entry -> {
            // nodes 1 & 2 are first and last
            // if (entry.getKey() > 2) {
            // I removed the previous condition cause nodes 1 & 2 are also critical
            // Color logic must be independent of critical path logic
            if (entry.getValue().getStart().equals(entry.getValue().getEnd())) {
                // nodes[n].color = {border:'#ff0000'}
                entry.getValue().setCritical(true);
            }
            // }
        });
    }

}
