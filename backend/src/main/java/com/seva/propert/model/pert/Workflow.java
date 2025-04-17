package com.seva.propert.model.pert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.seva.propert.exception.WorkFlowLoopException;
import com.seva.propert.model.entity.Task;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Workflow {

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

    /**
     * Constructor for the workflow class
     * @param tasks The list of tasks to be included in the workflow
     */
    public Workflow(List<Task> tasks) {
        this.tasks = tasks.stream()
            .collect(Collectors.toMap(Task::getId, TaskElement::fromTask));
        firstNode = new Node(nodeCounter++, "I");
        lastNode = new Node(nodeCounter++, "F");
        nodes.put(firstNode.getId(), firstNode);
        nodes.put(lastNode.getId(), lastNode);
    }

    public void checkAndInitialize() throws WorkFlowLoopException {
        if (hasLoop()) throw new WorkFlowLoopException();
        initialize();
    }

    
    private void initialize() {
        // Net calculations
        calcNextTasks();     // First calculate dependencies (we have only the predecessors)
        createDummyTasks();  // Then look for conflicted tasks and resolve tham by adding dummy tasks
        calcEdgesAndNodes(); // Create the edges & nodes collection
        nestNodes();         // Associate the nodes by the edges

        // PERT specific calculations
        calcNodesTimes(); // Calculate the starting & finalizing times of nodes
        calcTasksTimes(); // Calculate the tasks(edges) times based on nodes times
        calcCriticPath(); // Calculate wich nodes and edges belong to the critical path
    }

    /**
     *  Recursive function to check if a task has a loop in its dependencies
     * 
     * @param task      The task to be checked
     * @param unvisited The set of unvisited tasks
     * @param inProcess The set of tasks in process
     * @return          True if a loop is detected, false otherwise
     */
    private boolean hasLoop(TaskElement task, Set<String> unvisited, Set<String> inProcess) {
        if (unvisited.contains(task.getId())) {
            inProcess.add(task.getId()); // Mientras la pongo en la lista de 'en proceso'
            // Busco si alguna dependencia también está en la lista inProcess,
            // si es asi es que hay un ciclo, devuelvo true
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
            inProcess.remove(task.getId());
            unvisited.remove(task.getId());
        }
        return false;
    }

    /**
     *  Evaluates if any of the tasks has an infinite loop in their relationships
     * 
     * @return
     */
    private boolean hasLoop() {
        Set<String> unvisited = new HashSet<>(tasks.keySet());
        Set<String> inProcess = new HashSet<>();

        for (TaskElement t : tasks.values()) {
            if (hasLoop(t, unvisited, inProcess)) return true;
        }
        return false;
    }

    /**
     *  Fora ech task builds the list of tasks that depends on it
     *  replicating the same as the predecessors list
     */
    private void calcNextTasks() {
        // Given the Cartesian product excluding the main diagonal
        // & taking the predecessors as reference,
        // add the task as a dependency of the dependant task
        tasks.forEach((t, task) -> tasks.forEach((d, dependency) -> {
            if (!t.equals(d) && !dependency.getPredecessors().isEmpty() && 
                dependency.getPredecessors().contains(t)) {
                task.addDependency(d);
            }
        }));
    }

    /**
     *  Order a list of tasks by their id
     *  and returns the list as a string
     * 
     * @param taskList The list of tasks to be ordered (comma separated)
     * @return         The list of tasks ordered by id
     */
    private String orderTasks(String taskList) {
        return Arrays.stream(taskList.split(","))
            .sorted()
            .collect(Collectors.joining(","));
    }

    /**
     * Check different situations on wich a conflict can be detected
     * & return the id of the task on which the conflict is detected
     * 
     * 1. Dependant tasks with different predecessors
     * 2. Dependant tasks with share some predecessors but others are different
     * 3. Dependant tasks with exactly the same predecessors & dependencies
     * 
     * @param dependencies The list of dependencies of a given task
     * @return The id of the task on which the conflict is detected
     */
    private String checkConflict(String dependencies) {
        String tasksWithDifferentPred = getTaskWithDifferentPredecessors(dependencies);
        Boolean tasksSharePred = tasksShareAnyPredecessor(dependencies);
        String tasksWithSamePath = getTasksWithSamePath(dependencies);

        // Here we look for tasks whose predecessors & dependencies match exactly
        if (tasksWithSamePath != null) {
            return tasksWithSamePath;
            // Here we look for tasks who share at least one predecessor but whose
            // predecesors are not exactly the same
        } else if (tasksWithDifferentPred != null && tasksSharePred) {
            return tasksWithDifferentPred;
        } else
            return null;
    }

    /**
     * Compare two tasks lists ignoring the order of the elements
     * This function uses sets for readability and consistence,
     * we could just order strings with orderTasks() and compare them
     * 
     * @param taskList1 The first path to be compared
     * @param taskList2 The second path to be compared
     * @return True if both task lists are the same independently of the order, false otherwise
     */
    private Boolean equalsIgnoreOrder(String taskList1, String taskList2) {
        Set<String> set1 = new HashSet<>(Arrays.asList(taskList1.split(",")));
        Set<String> set2 = new HashSet<>(Arrays.asList(taskList2.split(",")));
        return set1.equals(set2);
    }

    // private Boolean intersect(String taskList1, String taskList2) {
    //     Set<String> set1 = new HashSet<>(Arrays.asList(taskList1.split(",")));
    //     Set<String> set2 = new HashSet<>(Arrays.asList(taskList2.split(",")));
    //     set1.retainAll(set2);
    //     return !set1.isEmpty();
    // }

    /**
     * Given a list of tasks, check if some of them has exactly the same
     * predecessors & dependencies
     * 
     * @param taskList The list of tasks to be checked
     * @return The id of the task on which the conflict is detected or null
     */
    private String getTasksWithSamePath(String taskList) {
        String[] taskIds = orderTasks(taskList).split(",");

        // Compares each dependant task with all the others
        for (String taskId : taskIds) {
            TaskElement task = tasks.get(taskId);

            for (String otherId : taskIds) {
                if (!taskId.equalsIgnoreCase(otherId)) {
                    TaskElement taskOther = tasks.get(otherId);
                    // If both the predecessors and dependencies are the same
                    // return the id of the task & stop processing
                    if (equalsIgnoreOrder(task.getPredecessors(), taskOther.getPredecessors()) &&
                            equalsIgnoreOrder(task.getDependencies(), taskOther.getDependencies()))
                        return taskId;
                }
            }
        }
        return null;
    }

    /**
     * Given a list of tasks, check if some of them has different predecessors
     * 
     * @param taskList The list of tasks to be checked
     * @return The id of the task on which the conflict is detected or null
     */
    private String getTaskWithDifferentPredecessors(String taskList) {
        String predecessors = "";
        Map<String, String> pred = new HashMap<>();
        Boolean different = false;

        String[] taskIds = orderTasks(taskList).split(",");
        for (String taskId : taskIds) {
            TaskElement task = tasks.get(taskId);
            String taskPredList = orderTasks(task.getPredecessors());

            // Save the predecessors in a map in reverse order
            // the key is the predecessors list and the value is the task id,
            // this way I can search later by a combination of predecessors
            log.debug("\t" + taskId + " predecessors: " + taskPredList);
            pred.put(taskPredList, taskId);

            if (predecessors.isEmpty()) {
                predecessors = taskPredList;
            } else if (!predecessors.equalsIgnoreCase(taskPredList)) {
                different = true;
                break;
            }
        }
        if (different) {
            Optional<String> largerList = pred.keySet().stream()
                    .max(Comparator.comparingInt(key -> key.split(",").length));

            // Return the one with the largest predecessors list
            log.debug("\tBigger: " + pred.get(largerList.get()) + " -> " + largerList.get());
            return largerList.isPresent() ? pred.get(largerList.get()) : null;
        }

        return null;
    }

    // Given a list of dependencies:
    // Check if some of them share exactly the same predecessors list
    private Boolean tasksShareAnyPredecessor(String taskList) {
        Set<String> predecessors = new HashSet<>();
        String[] taskIds = orderTasks(taskList).split(",");

        // Compare each task in the list with all the others
        for (String taskId : taskIds) {
            TaskElement task = tasks.get(taskId);
            String[] taskPred = orderTasks(task.getPredecessors()).split(",");

            for (String predId : taskPred) {
                // Check if the predecessor ID is already in the set
                if (predecessors.contains(predId))
                    return true;
                else {
                    // Add the predecessor ID to the set
                    predecessors.add(predId);
                }
            }
        }
        return false;
    }

    // private String remove(String list, String item) {
    // return Arrays.stream(list.split(","))
    // .filter(element -> !element.equalsIgnoreCase(item))
    // .map(String::toString)
    // .collect(Collectors.joining(","));
    // }

    private void createDummyTasks() {
        for (TaskElement task : new ArrayList<>(tasks.values())) {
            log.debug("Task: " + task.getId());

            // If there are task wich depends on task
            if (!task.getDependencies().isEmpty()) {
                int counter = 1;

                // Initialize the conflict flag
                String conflictTaskId = checkConflict(task.getDependencies());
                while (conflictTaskId != null) {
                    log.debug("-- Cicle:" + counter++ + "--");

                    TaskElement conflictTask = tasks.get(conflictTaskId);
                    log.debug("\tTask " + task.getId() + " has conflicts with dependency: " + conflictTaskId);

                    String dummyTaskId = "D" + dummyTaskCounter++;
                    TaskElement dummyTask = new TaskElement(dummyTaskId,
                            "Dummy task",
                            0d,
                            task.getId(),
                            true, -1d, -1d, -1d, -1d,
                            conflictTaskId);
                    log.debug("\t\tDummy task created: " + dummyTaskId);

                    tasks.put(dummyTaskId, dummyTask);

                    // Literally put the dummy task in middle of task & its dependencies
                    log.debug(String.format("\t\tTask: %s replace predecessors: %s x %s", task.getId(), conflictTaskId,
                            dummyTaskId));
                    task.replaceDependency(conflictTaskId, dummyTaskId);
                    conflictTask.replacePredecessor(task.getId(), dummyTaskId);

                    // Recursivelly check if the conflicted task has a new conflict
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

    /**
     * Given a task and the previous task's id, set the source node of the edge
     * 
     * @param task The task for which we want to add an origin
     * @param prev The previous task's id
     */
    private void edgeAddOrigin(TaskElement task, String prev) {
        Edge taskEdge = edges.get(task.getId());

        // If the prev task has an edge created already
        if (edges.containsKey(prev)) {
            Edge prevTaskEdge = edges.get(prev);

            // If the prev task has a target node set, then that node is the source node of
            // the taskEdge
            if (prevTaskEdge.getTo() != null) {
                taskEdge.setFrom(prevTaskEdge.getTo());
            }
        } else {
            // No edge created yet for the prev task in calcEdgesAndNodes, so..

            if (taskEdge.getFrom() == null) {
                // As the target node of every task predecessor is the source node of taskEdge:
                // If there's an edge created for the predecessor and its target node is set
                // then that target node is the source node of the taskEdge
                Node sourceNode = Arrays.stream(task.getPredecessors().split(","))
                        .filter(predId -> edges.containsKey(predId) && edges.get(predId).getTo() != null)
                        .map(predId -> edges.get(predId).getTo())
                        .findFirst()
                        .orElse(null);

                if (sourceNode == null) {
                    TaskElement prevTask = tasks.get(prev);

                    // If still no node has been found, i can look for the task siblings, that is
                    // it can be searched by the source nodes of the dependencies of the previous
                    // task
                    sourceNode = Arrays.stream(prevTask.dependencies.split(","))
                            .filter(depId -> edges.containsKey(depId) && edges.get(depId).getFrom() != null)
                            .map(depId -> edges.get(depId).getFrom())
                            .findFirst()
                            .orElse(null);
                }
                // Only if none of the task predecessors nor the prev task dependencies has
                // a reference to the source node, then create a new node
                if (sourceNode == null) {
                    Long newNodeId = nodeCounter++;
                    sourceNode = new Node(newNodeId, newNodeId.toString());
                    nodes.put(newNodeId, sourceNode);
                }

                taskEdge.setFrom(sourceNode);
            }
        }
    }

    /**
     * Given a task and the next task's id, set the target node of the edge
     * 
     * @param task The task for which we want to add a destination
     * @param next The next task's id
     */
    private void edgeAddDestination(TaskElement task, String next) {
        Edge taskEdge = edges.get(task.getId());

        // If the next task has an edge created already
        if (edges.containsKey(next)) {
            Edge nextTaskEdge = edges.get(next);

            // If the next task has a source node set, then that node is the target node of
            // the taskEdge
            if (nextTaskEdge.getFrom() != null) {
                taskEdge.setTo(nextTaskEdge.getFrom());
            }
        } else {
            // No edge created yet for the next task in calcEdgesAndNodes, so..

            // I don't know if this is necessary
            if (taskEdge.getTo() == null) {
                // Before creating a new node we should check if there is another of the
                // dependencies
                // that is already created, only if none has the from set I can create a node

                // As the source node of every task dependecy is the target node of taskEdge:
                // If there's an edge created for the dependency and its source node is set
                // then that source node is the target node of the taskEdge
                Node targetNode = Arrays.stream(task.getDependencies().split(","))
                        .filter(depId -> edges.containsKey(depId) && edges.get(depId).getFrom() != null)
                        .map(depId -> edges.get(depId).getFrom())
                        .findFirst()
                        .orElse(null);

                // If still no node has been found i can llok for the task siblings, that is
                // it can be searched by the target nodes of the predecessors of the next task
                if (targetNode == null) {
                    TaskElement nextTask = tasks.get(next);

                    targetNode = Arrays.stream(nextTask.predecessors.split(","))
                            .filter(predId -> edges.containsKey(predId) && edges.get(predId).getTo() != null)
                            .map(predId -> edges.get(predId).getTo())
                            .findFirst()
                            .orElse(null);
                }

                // Only if none of the task dependencies nor the next task predecessors has
                // a reference to the target node, then create a new node
                if (targetNode == null) {
                    Long newNodeId = nodeCounter++;
                    targetNode = new Node(newNodeId, newNodeId.toString());
                    nodes.put(newNodeId, targetNode);
                }

                taskEdge.setTo(targetNode);
            }
        }
    }

    /**
     *  Calculates the edges and nodes of the workflow
     *  based on the tasks predecessors & dependencies
     *  creates the edges and then links them with the nodes
     *  creating nodes when necessary
     */
    private void calcEdgesAndNodes() {
        tasks.forEach((t, task) -> {
            log.debug(task.getId());
            if (!edges.containsKey(t)) {
                String labelText = String.format(Locale.ROOT,"%s (%.1f)", t, task.getLength());
                Edge newEdge = new Edge(t, labelText, null, null, false);
                edges.put(t, newEdge);
            }

            // Source Nodes
            if (task.getPredecessors().isEmpty()) {
                edges.get(t).setFrom(firstNode);
            } else {
                log.debug("\t" + task.getPredecessors() + " -> " + task.getId());
                // For each element in prev
                String[] predecessors = orderTasks(task.getPredecessors()).split(",");
                for (String p : predecessors)
                    edgeAddOrigin(task, p);
            }

            log.debug("------------");

            // Target Nodes
            if (task.getDependencies().isEmpty()) {
                edges.get(t).setTo(lastNode);
            } else {
                log.debug("\t" + task.getId() + " -> " + task.getDependencies());
                // Para c/u de los elementos en next
                String[] dependencies = orderTasks(task.getDependencies()).split(",");
                for (String n : dependencies)
                    edgeAddDestination(task, n);
            }
        });
    }

    /**
     * For each edge include it in the "from" node's "next" collection
     * and in the "to" node's "previous" collection. Resulting in The
     * nodes being nested by the edges
     */
    private void nestNodes() {
        for (Edge edge : edges.values()) {
            edge.getFrom().getNext().put(edge.getId(), edge);
            edge.getTo().getPrevious().put(edge.getId(), edge);
        }
    }

    private Double round(Double value, int scale) {
        if (value == null) return null;
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(scale, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Given a map of edges calculates the maximum of the task's early starts
     * 
     * @param eds The map containing the edges
     * @return The maximum of the task's early starts
     */
    private Double getMaxLength(Map<String, Edge> eds) {
        Double max = 0d;
        int scale = 10; // Number of decimal places to round to

        for (Edge e : eds.values()) {
            Double newValue = getTasksEarlyStart(e.getFrom()) + tasks.get(e.getId()).getLength();
            newValue = round(newValue, scale);

            if (newValue > max) max = newValue;
        }
        return max;
    }

    /**
     * Given a map of edges calculates the minimum of the task's late finishs
     * 
     * @param eds The map containing the edges
     * @return The minimum of the task's late finishs
     */
    private Double getMinLength(Map<String, Edge> eds) {
        Double min = Double.MAX_VALUE;
        int scale = 10; // Number of decimal places to round to

        for (Edge e : eds.values()) {
            Double newValue = getTasksLateFinish(e.getTo()) - tasks.get(e.getId()).getLength();
            // Round the result to avoid very small values
            newValue = round(newValue, scale);

            if (newValue < min) min = newValue;
        }
        return min;
    }

    /**
     * @param node The node for which we want to calculate the earliest start
     * @return The node earliest start
     */
    private Double getTasksEarlyStart(Node node) {
        if (node.getPrevious().isEmpty())
            return 0d;
        return getMaxLength(node.getPrevious());
    }

    /**
     * @param node The node for which we want to calculate the latest finish
     * @return The node latest finish
     */
    private Double getTasksLateFinish(Node node) {
        if (node.getNext().isEmpty())
            return node.getStart();
        return getMinLength(node.getNext());
    }

    /**
     * Fora each node calculates the starting & finalizing times
     */
    private void calcNodesTimes() {
        // Calculate each node Early Start
        nodes.values().forEach(n -> {
            n.setStart(getTasksEarlyStart(n));
            log.debug("Node " + n.getId() + " ES: " + n.getStart());
        });
        nodes.values().forEach(n -> {
            n.setEnd(getTasksLateFinish(n));
            log.debug("Node " + n.getId() + " EF: " + n.getEnd());
        });
    }

    /**
     * @param task TaskElement correspondint to the task
     * @return The task early start
     */
    private Double getTaskEarlyStart(TaskElement task) {
        Edge edge = edges.get(task.getId());
        return edge.getFrom().getStart();
    }

    /**
     * @param task TaskElement correspondint to the task
     * @return The task late finish
     */
    private Double getTaskLateFinish(TaskElement task) {
        Edge edge = edges.get(task.getId());
        return edge.getTo().getEnd();
    }

    /**
     * Calculates the tasks based on the nodes times
     */
    private void calcTasksTimes() {

        // This order is good, first we need to get the early starts & early finish to
        // calculate then the latest times
        tasks.values().forEach(t -> {
            // Calculate early start & early finish
            t.setEarlyStart(getTaskEarlyStart(t));
            Double earlyFinish = round(t.getEarlyStart() + t.getLength(),10);
            t.setEarlyFinish(earlyFinish);
            log.debug("Task " + t.getId() + " ES: " + t.getEarlyStart() + " EF: " + t.getEarlyFinish());
        });

        // Calculate latest finish & latest start
        tasks.values().forEach(t -> {
            t.setLateFinish(getTaskLateFinish(t));
            Double lateStart = round(t.getLateFinish() - t.getLength(),10);
            t.setLateStart(lateStart);
            log.debug("Task " + t.getId() + " LS: " + t.getLateStart() + " LF: " + t.getLateFinish());
        });
    }

    /**
     * Flag the edges & nodes belonging to the critical path
     */
    private void calcCriticPath() {

        // Tareas del camino crítico
        tasks.values().forEach(t -> {
            Boolean isCritical = t.getEarlyStart().equals(t.getLateStart()) &&
                    t.getEarlyFinish().equals(t.getLateFinish());
            edges.get(t.getId()).setCritical(isCritical);
        });

        // Nodos del camino crítico
        nodes.values().stream()
                .filter(node -> node.getStart().equals(node.getEnd()))
                .forEach(n -> n.setCritical(true));
    }

}
