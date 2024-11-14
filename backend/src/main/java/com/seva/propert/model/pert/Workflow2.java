package com.seva.propert.model.pert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.seva.propert.model.entity.Task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Workflow2 {
    @Data
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    private class TaskElement {
        protected String id;
        protected String description;
        protected Double length;

        protected String predecessors = "";

        protected Boolean dummy = false;

        protected Double earlyStart = -1d;
        protected Double earlyFinish = -1d;
        protected Double lateStart = -1d;
        protected Double lateFinish = -1d;

        protected String dependencies = "";

        public void addDependency(String dep) {
            this.dependencies += (dependencies.length() == 0 ? "" : ",").concat(dep);
        }

        public void replacePredecessor(String oldTaskId, String newTaskId) {
            this.predecessors = this.predecessors.replace(oldTaskId, newTaskId);
        }

        public void replaceDependency(String oldTaskId, String newTaskId) {
            this.dependencies = this.dependencies.replace(oldTaskId, newTaskId);
        }

    }

    @JsonIgnore
    private Map<String, TaskElement> tasks = null;
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

    public Workflow2(List<Task> tasks) {
        this.tasks = tasks.stream()
                .collect(Collectors.toMap(Task::getId, task -> taskToTaskElement(task)));
        firstNode = new Node(nodeCounter++, "I");
        lastNode = new Node(nodeCounter++, "F");
        nodes.put(firstNode.getId(), firstNode);
        nodes.put(lastNode.getId(), lastNode);
        initialize();
    }

    private TaskElement taskToTaskElement(Task t) {
        TaskElement element = new TaskElement();
        element.id = t.getId();
        element.description = t.getDescription();
        element.length = t.getLength();

        element.predecessors = t.getPredecessors().stream()
                .map(Task::getId)
                .collect(Collectors.joining(","));
        element.dependencies = t.getDependencies().stream()
                .map(Task::getId)
                .collect(Collectors.joining(","));

        return element;
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
    private Boolean hasLoop(TaskElement task, Set<String> unvisited, Set<String> inProcess) {
        if (unvisited.contains(task.getId())) {
            inProcess.add(task.getId()); // Mientras la pongo en la lista de 'en proceso'
            // Busco si alguna dependencia también está en la lista inProcess,
            // si es asi es que hay un ciclo, devuelvo true;
            if (!task.getPredecessors().isEmpty()) {
                // Recorro la lista de ids
                String[] predecessors = task.getPredecessors().split(",");
                for (String depTask : predecessors) {
                    if (inProcess.contains(depTask)) {
                        return true;
                    } else {
                        // Recorro de forma recursiva
                        if (hasLoop(tasks.get(depTask), unvisited, inProcess))
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

        for (TaskElement t : tasks.values()) {
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
                    if (dependency.getPredecessors().contains(t)) {
                        task.setDependencies(task.getDependencies()
                                .concat(task.getDependencies().length() == 0 ? "" : ",").concat(d));
                    }
                }
            });
        });
    }

    private String orderTasks(String taskList) {
        String[] array = taskList.split(",");
        Arrays.sort(array);
        return String.join(",", array);
    }

    private String getDepWithDifferentPredecessors(String dependencies) {
        String predecessors = "";
        //En este mapa se guardaran las tareas junto con sus predecesores
        Map<String, String> pred = new HashMap<>();
        Boolean different = false;

        String[] dep = orderTasks(dependencies).split(",");
        for (String taskId : dep) {
            TaskElement task = tasks.get(taskId);
            String taskPred = orderTasks(task.getPredecessors());

            //Lo guardo al revés, la clave como valor y el valor como clave
            //de esta forma puedo buscar luego por una combinación de tareas predecesoras
            //Si es la misma no importa porque cualquiera me viene bien
            log.info("taskPred: " +taskPred);
            pred.put(taskPred, taskId);

            log.info("\t\t" + taskId + " -> " + taskPred);
            if (predecessors.length() == 0) {
                predecessors = taskPred;
            } else if (!predecessors.equalsIgnoreCase(taskPred)) {
                // return taskId;
                different = true;
                break;
            }
        }
        if (different) {
            Optional<String> bigger = pred.keySet().stream()
            .max( Comparator.comparingInt(String::length));
            //.collect(Collectors.joining(","));
            log.info("Bigger: " + bigger.get());
            return bigger.isPresent() ?  pred.get(bigger.get()) : null;
        }
        return null;
    }

    // private synchronized Boolean depWithSamePredecessors(List<Task>
    // dependencies){
    // // Map<String, Task> dependencies = new HashMap<>();
    // String predecessors = "";
    // for(Task t : dependencies){
    // log.info("\t\t" + t.getId() + " -> " + listAsString(t.getPredecessors()));
    // String taskDep = listAsString(t.getPredecessors());

    // if (predecessors.length() == 0) {
    // predecessors = taskDep;
    // } else if (!predecessors.equalsIgnoreCase(taskDep)) return false;
    // }
    // return true;
    // }

    private Boolean dependenciesShareAnyPredecessor(String dependencies) {
        String predecessors = "";
        String[] dep = orderTasks(dependencies).split(",");
        for (String taskId : dep) {
            TaskElement task = tasks.get(taskId);
            String[] taskPred = orderTasks(task.getPredecessors()).split(",");

            for (String predId : taskPred) {
                if (predecessors.contains(predId))
                    return true;
                else
                    predecessors += (predecessors.length() == 0 ? "" : ",").concat(predId);
            }
        }
        return false;
    }

    private String remove(String list, String item) {
        return Arrays.stream(list.split(","))
                .filter(element -> !element.equalsIgnoreCase(item))
                .map(String::toString)
                .collect(Collectors.joining(","));
    }

    private void createDummyTasks() {
        for (TaskElement task : new ArrayList<>(tasks.values())) {
            log.info("Task: " + task.getId());

            int counter = 0;
            // If there are task wich depends on task
            if (!task.getDependencies().isEmpty()) {

                // Mientras haya tareas en conflicto seguir en el bucle para solucionarlo
                String conflictTaskId = getDepWithDifferentPredecessors(task.getDependencies());
                while (conflictTaskId != null &&
                        dependenciesShareAnyPredecessor(task.getDependencies()) && counter < 15) {

                    log.info("\tTask " + task.getId() + " has dependencies with different predecessors");

                    TaskElement conflictTask = tasks.get(conflictTaskId);// nextTasks.next();
                    log.info("\t\tDep: " + conflictTaskId);

                    String dummyTaskId = "D" + dummyTaskCounter++;
                    TaskElement dummyTask = new TaskElement(dummyTaskId,
                            "Dummy task",
                            0d,
                            task.getId(),
                            true, -1d, -1d, -1d, -1d,
                            conflictTaskId);
                    log.info("\t\tCreo dummy: " + dummyTaskId);

                    tasks.put(dummyTaskId, dummyTask);

                    // Literally i put the dummy task in middle of task & its dependencies
                    log.info("\t\tTask: " + conflictTask.getId() + " cambio predecesores: " + task.getId() + " x "
                            + dummyTaskId);
                    task.replaceDependency(conflictTaskId, dummyTaskId);
                    log.info("**" + conflictTask.getPredecessors());
                    conflictTask.replacePredecessor(task.getId(), dummyTaskId);
                    log.info("**" + conflictTask.getPredecessors());

                    conflictTaskId = getDepWithDifferentPredecessors(task.getDependencies());
                    counter++;
                }
            }
        }
    }

    private void edgeAddOrigin(TaskElement task, String prev) {

        Edge edgeTask = edges.get(task.getId());
        Edge edgePrev = edges.get(prev);

        if (edges.containsKey(prev)) {

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

    private void edgeAddDestination(TaskElement task, String next) {
        Edge edgeTask = edges.get(task.getId());
        Edge edgeNext = edges.get(next);

        if (edges.containsKey(next)) {

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
                String[] predecessors = orderTasks(task.getPredecessors()).split(",");
                for (String p : predecessors)
                    edgeAddOrigin(task, p);
            }

            // NEXT
            if (task.getDependencies().isEmpty()) {
                edges.get(t).setTo(lastNode);
                // log.info("Se conecta el edge " + t + " al nodo final");
            } else {
                // Para c/u de los elementos en next
                String[] dependencies = orderTasks(task.getDependencies()).split(",");
                for (String n : dependencies)
                    edgeAddDestination(task, n);
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
    private Double getTaskEarlyStart(TaskElement task) {
        Edge edge = edges.get(task.getId());
        return edge.getFrom().getStart();
    }

    // Fin del nodo de destino
    private Double getTaskLateFinish(TaskElement task) {
        Edge edge = edges.get(task.getId());
        return edge.getTo().getEnd();
    }

    private void calcTasksTimes() {

        // This order is good, first we need to get the early starts & early finish to
        // calculate then the latest times
        tasks.values().forEach((t) -> {
            // Calculate early start & early finish
            t.setEarlyStart(getTaskEarlyStart(t));
            t.setEarlyFinish(t.getEarlyStart() + t.getLength());
        });

        tasks.values().forEach((t) -> {
            // Calculate latest finish & latest start
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

        // nodes.entrySet().forEach(entry -> {
        // // nodes 1 & 2 are first and last
        // // if (entry.getKey() > 2) {
        // // I removed the previous condition cause nodes 1 & 2 are also critical
        // // Color logic must be independent of critical path logic
        // if (entry.getValue().getStart().equals(entry.getValue().getEnd())) {
        // // nodes[n].color = {border:'#ff0000'}
        // entry.getValue().setCritical(true);
        // }
        // // }
        // });

        // Nodos del camino crítico
        nodes.values().stream()
                .filter(node -> node.getStart().equals(node.getEnd()))
                .forEach(n -> n.setCritical(true));
    }

}
