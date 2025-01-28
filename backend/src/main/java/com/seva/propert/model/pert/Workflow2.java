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
import com.seva.propert.exception.WorkFlowLoopException;
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

        //Changed these two method's behaviour because replacing strings leads to errors
        //If i replace task D for X and we have a task D2 wi will end in X2
        //Now i search for entire elements
        public void replacePredecessor(String oldTaskId, String newTaskId) {
            String[] elements = this.predecessors.split(",");

            this.predecessors = Arrays.stream(elements)
            .map(element -> element.equals(oldTaskId) ? newTaskId : element) // Reemplazar solo si coincide
            .collect(Collectors.joining(","));
        }

        public void replaceDependency(String oldTaskId, String newTaskId) {
            String[] elements = this.dependencies.split(",");

            this.dependencies = Arrays.stream(elements)
            .map(element -> element.equals(oldTaskId) ? newTaskId : element) // Reemplazar solo si coincide
            .collect(Collectors.joining(","));
        }

    }

    @JsonIgnore
    private Map<String, TaskElement> tasks = null;
    // @JsonIgnore
    private Map<Long, Node> nodes = new HashMap<>();
    // @JsonIgnore
    private Map<String, Edge> edges = new HashMap<>();

    // @JsonProperty("nodes")
    // private List<Node> nds;
    // @JsonProperty("edges")
    // private List<Edge> eds;

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
        //initialize();
        // nds = nodes.values().stream().collect(Collectors.toList());
        // eds = edges.values().stream().collect(Collectors.toList());
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

    public void checkAndInitialize() throws WorkFlowLoopException {
        if (hasLoop()) throw new WorkFlowLoopException();
        initialize();
    }

    private void initialize() {
        calcNextTasks();          //First calculate dependencies (we have only the predecessors)
        createDummyTasks();       //Then look for conflicted tasks and resolve tham by adding dummy tasks
        calcEdgesAndNodes();      //Create the edges & nodes collection
        nestNodes();              //Associate the nodes by the edges
        //PERT calculations
        calcNodesTimes();         //Calculate the starting & finalizing times of nodes
        calcTasksTimes();         //Calculate the tasks(edges) times based on nodes times
        calcCriticPath();         //Calculate wich nodes and edges belong to the critical path
    }

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

    //Check different situations on wich 
    private String checkConflict(String dependencies) {
        String tasksWithDifferentPred = getDepWithDifferentPredecessors(dependencies);
        Boolean tasksSharePred = dependenciesShareAnyPredecessor(dependencies);
        String tasksWithSamePath = getTasksWithSamePath(dependencies);

        //Here we look for tasks whose predecessors & dependencies match exactly
        if (tasksWithSamePath != null) {
            return tasksWithSamePath;
        //Here we look for tasks who share at least one predecessor but whose predecesors are not exactly the same
        } else if (tasksWithDifferentPred != null && tasksSharePred) {
            return tasksWithDifferentPred;
        } else return null;
    }

    //This function uses sets for readability and consistence, 
    //we could just order strings with orderTasks() and compare them
    private Boolean equalsIgnoreOrder(String path1, String path2) {
        Set<String> set1 = new HashSet<>(Arrays.asList(path1.split(",")));
        Set<String> set2 = new HashSet<>(Arrays.asList(path2.split(",")));
        return set1.equals(set2);
    }

    private String getTasksWithSamePath(String dependencies) {
        //Each dependent task is compared with all the others for coincidences
        String[] dep = orderTasks(dependencies).split(",");
        for (String taskId : dep) {
            TaskElement task = tasks.get(taskId);

            // String taskPred = orderTasks(task.getPredecessors());
            // String taskDep = orderTasks(task.getDependencies());

            for (String otherId : dep) {
                if (!taskId.equalsIgnoreCase(otherId)){
                     TaskElement taskOther = tasks.get(otherId);
                     if (equalsIgnoreOrder(task.getPredecessors(), taskOther.getPredecessors()) &&
                         equalsIgnoreOrder(task.getDependencies(), taskOther.getDependencies())) return taskId;

                    // if (taskPred.equalsIgnoreCase(orderTasks(taskOther.getPredecessors())) &&
                    //     taskDep.equalsIgnoreCase(orderTasks(taskOther.getDependencies()))) {
                    //     return taskId;
                    // }
                }
            }
        }
        return null;
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
            log.debug("\t" + taskId + " predecessors: " + taskPred);
            pred.put(taskPred, taskId);

            if (predecessors.length() == 0) {
                predecessors = taskPred;
            } else if (!predecessors.equalsIgnoreCase(taskPred)) {
                // return taskId;
                different = true;
                log.debug("\tCorta el recorrido de las dependencias");
                break;
            }
        }
        if (different) {
            Optional<String> bigger = pred.keySet().stream()
            .max( Comparator.comparingInt(String::length));
            //.collect(Collectors.joining(","));
            log.debug("\tBigger: " + pred.get(bigger.get()) + " -> " + bigger.get());
            return bigger.isPresent() ?  pred.get(bigger.get()) : null;
        }
        return null;
    }

    //Given a list of dependencies:
    //Check if some of them share exactly the same predecessors list
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

    //This is the main source of bugs cause it is the more complex function
    private void createDummyTasks() {
        for (TaskElement task : new ArrayList<>(tasks.values())) {
            log.debug("Task: " + task.getId());

            // If there are task wich depends on task
            if (!task.getDependencies().isEmpty()) {
                int counter = 1;

                String conflictTaskId = checkConflict(task.getDependencies());
                while(conflictTaskId != null) {
                    log.debug("-- Ciclo:" + counter++ + "--");

                    TaskElement conflictTask = tasks.get(conflictTaskId);// nextTasks.next();
                    log.debug("\tTask " + task.getId() + " tiene conflicto con dedendencia: " + conflictTaskId);

                    String dummyTaskId = "D" + dummyTaskCounter++;
                    TaskElement dummyTask = new TaskElement(dummyTaskId,
                            "Dummy task",
                            0d,
                            task.getId(),
                            true, -1d, -1d, -1d, -1d,
                            conflictTaskId);
                    log.debug("\t\tCreo dummy: " + dummyTaskId);

                    tasks.put(dummyTaskId, dummyTask);

                    // Literally i put the dummy task in middle of task & its dependencies
                    log.debug("\t\tTask: " + conflictTask.getId() + " cambio predecesores: " + task.getId() + " x "
                            + dummyTaskId);
                    task.replaceDependency(conflictTaskId, dummyTaskId);
                    conflictTask.replacePredecessor(task.getId(), dummyTaskId);

                    conflictTaskId = checkConflict(task.getDependencies());
                }
            }
        }
        log.debug("------- Tasks ------");
        for (TaskElement task : new ArrayList<>(tasks.values())) {
            log.debug(task.getId() + " PRED -> " + task.getPredecessors());
            log.debug(task.getId() + " DEP  -> " + task.getDependencies());
        }
    }

    private void edgeAddOrigin(TaskElement task, String prev) {

        Edge edgeTask = edges.get(task.getId());

        if (edges.containsKey(prev)) {
            Edge edgePrev = edges.get(prev);

            if (edgePrev.getTo() != null) {
                edgeTask.setFrom(edgePrev.getTo());
                // log.info(edgePrev.getTo().getId() + " -> " + task.getId());
            } else {
                //Aquí no debería entrar nunca porque cuando se crea la arista inmediatamente despues 
                //se asigna a un nodo
            }

        } else {
            // Creo un nuevo nodo y se lo asigno al campo to
            if (edgeTask.getFrom() == null) {
                //Antes de crear un nuevo nodo tengo que ver si hay otro de los predecesores que si 
                //estan creados, solo si ninguno tiene el to establecido puedo crear un nodo
                String found = "";
                for(String t : task.getPredecessors().split(",")) {
                    if (!t.equalsIgnoreCase(prev)){
                        if (edges.containsKey(t) && edges.get(t).getTo() != null) {
                            found = t;
                            break;
                        }
                    }
                }
                if (found.equals("")) {
                    Long newNodeId = nodeCounter++;
                    Node newNode = new Node(newNodeId, newNodeId.toString());
                    nodes.put(newNodeId, newNode); // { id: newNode, label: newNode.toString(), start:0, end:0, next:[],
                                                // prev:[]};
                    edgeTask.setFrom(newNode);
                    log.debug("add origin: Nodo " + newNodeId + " creado.");
                }
            } else {
                //Aquí no hace falta hacer nada, entra cuando hay varios predecesores,
                //el primero crea un nodo y luego los siguientes no necesitan ser procesados
            }
        }
    }

    private void edgeAddDestination(TaskElement task, String next) {
        Edge edgeTask = edges.get(task.getId());

        //Si existe una arista para la tarea siguiente
        if (edges.containsKey(next)) {
            Edge edgeNext = edges.get(next);

            // Si esta tiene definido un nodo 'from'
            // entonces lo tomo como nodo 'to' de esta
            if (edgeNext.getFrom() != null) {
                edgeTask.setTo(edgeNext.getFrom());
                // log.info(task.getId() + " -> " + edgeNext.getFrom().getId());
            } else {
                //Aquí no debería entrar nunca porque cuando se crea la arista inmediatamente despues 
                //se asigna a un nodo

                // // Creo un nuevo nodo y se lo asigno al campo to
                // Long newNodeId = nodeCounter++;
                // Node newNode = new Node(newNodeId, newNodeId.toString());
                // edgeTask.setTo(newNode);
                // edgeNext.setFrom(newNode);
                // log.info(task.getId() + " -> " + newNodeId);
            }

        } else {
            //Aquí entra porque no existe todavía una arista creada en el método calcEdgesAndNodes
            //Si no existe un edge creado para la siguiente tarea y
            // el campo To de la arista es nulo, tengo que crear un nodo y asignarselo
            if (edgeTask.getTo() == null) {
                //Antes de crear un nuevo nodo tengo que ver si hay otra de las dependencias que si 
                //este creada, solo si ninguna tiene el from establecido puedo crear un nodo
                Long foundNode = 0L;
                for(String t : task.getDependencies().split(",")) {
                    if (!t.equalsIgnoreCase(next)){
                        if (edges.containsKey(t) && edges.get(t).getFrom() != null) {
                            foundNode = edges.get(t).getFrom().getId();
                            break;
                        }
                    }
                }
                //Si aun no se ha encontrado un nodo asignado a una arista
                //todavía se puede buscar por los nodos destino de las precedencias
                //de la dependencia
                TaskElement nextTask = tasks.get(next);
                for(String t : nextTask.predecessors.split(",")) {
                    if (edges.containsKey(t) && edges.get(t).getTo() != null) {
                        foundNode = edges.get(t).getTo().getId();
                        break;
                    }
                }

                if (foundNode == 0L) {
                    // Creo un nuevo nodo y se lo asigno al campo to
                    Long newNodeId = nodeCounter++;
                    Node newNode = new Node(newNodeId, newNodeId.toString());
                    nodes.put(newNodeId, newNode);
                    edgeTask.setTo(newNode);
                    log.debug("add destination: Nodo " + newNodeId + " creado.");
                } else edgeTask.setTo(nodes.get(foundNode));
                
            } else {
                //Aquí no hace falta hacer nada, entra cuando hay varias dependencias,
                //la primera crea un nodo y luego las siguientes no necesitan ser procesadas
            }
        }
    }

    private void calcEdgesAndNodes() {
        tasks.forEach((t, task) -> {
            log.debug(task.getId());
            // log.info("task: ".concat(t));
            if (!edges.containsKey(t)) {
                Edge newEdge = new Edge(t, t + " (" + task.getLength() + ")", null, null, false);
                edges.put(t, newEdge);
                // log.info("Se agrega edge " + t);
            }

            // PREVIOUS
            if (task.getPredecessors().isEmpty()) {
                edges.get(t).setFrom(firstNode);
                // log.info("Se conecta el edge " + t + " al nodo inicial");
            } else {
                log.debug("\t" + task.getPredecessors() + " -> " + task.getId());
                // Para c/u de los elementos en predecessors
                String[] predecessors = orderTasks(task.getPredecessors()).split(",");
                for (String p : predecessors)
                    edgeAddOrigin(task, p);
            }

            log.debug("------------");

            // NEXT
            if (task.getDependencies().isEmpty()) {
                edges.get(t).setTo(lastNode);
                // log.info("Se conecta el edge " + t + " al nodo final");
            } else {
                log.debug("\t" + task.getId() + " -> " + task.getDependencies());
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
