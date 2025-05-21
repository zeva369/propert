package com.sidus.propert.service;

import com.sidus.propert.context.ErrorMessages;
import com.sidus.propert.converter.*;
import com.sidus.propert.dto.TaskDTO;
import com.sidus.propert.exception.DuplicatedElementException;
import com.sidus.propert.exception.ElementNotFoundException;
import com.sidus.propert.model.entity.Project;
import com.sidus.propert.model.entity.Task;
import com.sidus.propert.repository.TaskRepository;
import com.sidus.propert.service.impl.TaskServiceImpl;
import com.sidus.propert.service.impl.UserServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

//    @InjectMocks
    private TaskServiceImpl taskService;

    private String invalidTaskId = "123-abc-456-def";
    private String validTaskId = "123-456-789";

    private Long invalidProjectId = -1L;
    private Long validProjectId = 3L;

    private String validDescriptionPattern = "#";
    private String invalidDescriptionPattern = "?";
    private Task task1;
    private List<Task> mockTasks;

    private GenericConversionService conversionService;
//    private ErrorMessages errorMessages;

    TaskServiceImplTest() {
        Project project = new Project();
        project.setId(validProjectId);

        mockTasks = new ArrayList<>();
        task1 = new Task();
        task1.setId("task1");
        task1.setDescription("Task #1");
        task1.setLength(5d);
        task1.setProject(project);
        task1.setIsDummy(false);
        mockTasks.add(task1);

        Task task2 = new Task();
        task2.setId("task2");
        task2.setDescription("Task -2-");
        task2.setLength(3d);
        task2.setProject(project);
        task2.setIsDummy(false);
        mockTasks.add(task2);

    }

    @BeforeEach
    void setUp() {
        conversionService = new DefaultConversionService();
        conversionService.addConverter(new TaskConverter());
        conversionService.addConverter(new TaskInDTOConverter());
        conversionService.addConverter(new TaskDTOConverter());
        conversionService.addConverter(new TaskDTOToTaskElementConverter());

        ErrorMessages errorMessages = new ErrorMessages();

        // Crear instancia de TaskServiceImpl con los beans y mocks
        taskService = new TaskServiceImpl(errorMessages, taskRepository, conversionService);
    }

    //FindAll Tests

    @Test
    @DisplayName("findAll should return a list of Tasks")
    void findAllShouldReturnAllTasks() {
        when(taskRepository.findAll()).thenReturn(mockTasks);

        List<TaskDTO> tasks = taskService.findAll();
        Assertions.assertEquals(mockTasks.size(), tasks.size());
        Assertions.assertEquals(mockTasks.get(0).getId(), tasks.get(0).id());
        Assertions.assertEquals(mockTasks.get(1).getId(), tasks.get(1).id());
    }

    @Test
    @DisplayName("findAll should return an Empty list of Tasks")
    void findAllShouldReturnEmptyList() {
        when(taskRepository.findAll()).thenReturn(new ArrayList<>());

        List<TaskDTO> tasks = taskService.findAll();
        Assertions.assertTrue(tasks.isEmpty());
    }

    //FindById Tests

    @Test
    @DisplayName("findById should return Empty")
    void findByIdShouldReturnEmpty() {
        when(taskRepository.findById(invalidTaskId)).thenReturn(Optional.empty());

        Optional<TaskDTO> task = taskService.findById(invalidTaskId);
        Assertions.assertTrue(task.isEmpty());
    }

    @Test
    @DisplayName("findById should return a valid Task")
    void findByIdShouldReturnAValidTask() {
        TaskDTO task1DTO = conversionService.convert(task1, TaskDTO.class);
           /*     new TaskDTO(
                task1.getId(),
                task1.getDescription(),
                task1.getLength(),
                task1.getProject().getId(),
                new ArrayList<>());*/

        when(taskRepository.findById(validTaskId)).thenReturn(Optional.of(task1));

        Optional<TaskDTO> task = taskService.findById(validTaskId);
        Assertions.assertTrue(task.isPresent());
        Assertions.assertEquals(task1DTO, task.get());
    }

    //FindByDescriptionContaining Tests

    @Test
    @DisplayName("findByDescriptionContaining should return a List with valid Tasks")
    void findByDescriptionContainingShouldReturnAListOfTasks() {
        TaskDTO task1DTO = conversionService.convert(task1, TaskDTO.class);
                /*new TaskDTO(
                task1.getId(),
                task1.getDescription(),
                task1.getLength(),
                task1.getProject().getId(),
                new ArrayList<>());*/
        when(taskRepository.findByDescriptionContaining(validDescriptionPattern))
                .thenReturn(List.of(task1));

        List<TaskDTO> tasks = taskService.findByDescriptionContaining(validDescriptionPattern);
        Assertions.assertFalse(tasks.isEmpty());
        Assertions.assertEquals(task1DTO, tasks.get(0));
    }

    @Test
    @DisplayName("findByDescriptionContaining should return an Empty List")
    void findByDescriptionContainingShouldReturnAnEmptyList() {
        when(taskRepository.findByDescriptionContaining(invalidDescriptionPattern))
                .thenReturn(new ArrayList<>());

        List<TaskDTO> tasks = taskService.findByDescriptionContaining(invalidDescriptionPattern);
        Assertions.assertTrue(tasks.isEmpty());
    }

    //FindByProjectId Tests

    @Test
    @DisplayName("findByProjectId should return a List with valid Tasks")
    void findByProjectIdShouldReturnAListOfTasks() {
        TaskDTO task1DTO = conversionService.convert(task1, TaskDTO.class);

        when(taskRepository.findByProjectId(validProjectId))
                .thenReturn(List.of(task1));
        when(taskRepository.findByProjectId(validProjectId))
                .thenReturn(mockTasks);

        List<TaskDTO> tasks = taskService.findByProjectId(validProjectId);
        Assertions.assertFalse(tasks.isEmpty());
        Assertions.assertEquals(mockTasks.size(), tasks.size());
        Assertions.assertEquals(task1DTO, tasks.get(0));
    }

    @Test
    @DisplayName("findByProjectId should return an Empty List")
    void findByProjectIdShouldReturnAnEmptyList() {
        when(taskRepository.findByProjectId(invalidProjectId))
                .thenReturn(new ArrayList<>());

        List<TaskDTO> tasks = taskService.findByProjectId(invalidProjectId);
        Assertions.assertTrue(tasks.isEmpty());
    }

    //Save Tests

    @Test
    @DisplayName("save should return a valid Task")
    void saveShouldReturnAValidTask() {
        when(taskRepository.save(task1)).thenReturn(task1);

        TaskDTO taskToSave = new TaskDTO(
                task1.getId(),
                task1.getDescription(),
                task1.getLength(),
                task1.getProject().getId(),
                new ArrayList<>());

        TaskDTO savedTask = taskService.save(taskToSave);
        Assertions.assertNotNull(savedTask);
        Assertions.assertEquals(task1.getId(), savedTask.id());
        Assertions.assertEquals(task1.getDescription(), savedTask.description());
    }

    /*
    @Test
    @DisplayName("create should throw an exception when ID is duplicate")
    void createShouldThrowExceptionWhenIDIsDuplicate() {
        TaskDTO duplicateTaskDTO = new TaskDTO(
                "task1",
                "Task #1",
                5d,
                validProjectId,
                new ArrayList<>());

        Task duplicateTask = new Task();
        duplicateTask.setId(duplicateTaskDTO.id());
        duplicateTask.setDescription("Task #1");
        duplicateTask.setLength(5d);

        // Simular que el repositorio ya contiene un Task con el mismo ID
        when(taskRepository.findById("task1")).thenReturn(Optional.of(duplicateTask));

        DuplicatedElementException exception = Assertions.assertThrows(DuplicatedElementException.class, () -> {
            taskService.create(duplicateTaskDTO);
        });

        Assertions.assertEquals("Duplicated element.", exception.getMessage());
    }

    @Test
    @DisplayName("create should return a valid Task")
    void createShouldReturnAValidTask() {
        TaskDTO newTaskDTO = new TaskDTO(
                "task3",
                "Task #3",
                5d,
                new ArrayList<>());

        Task newTask = new Task();
        newTask.setId("task3");
        newTask.setDescription("Task #3");
        newTask.setLength(5d);
        newTask.setIsDummy(false);

        // Simular que el repositorio no contiene un Task con el mismo ID
        when(taskRepository.findById("task3")).thenReturn(Optional.empty());
        when(taskRepository.save(newTask)).thenReturn(newTask);

        TaskDTO createdTask = taskService.create(newTaskDTO);
        Assertions.assertNotNull(createdTask);
        Assertions.assertEquals(newTaskDTO.id(), createdTask.id());
        Assertions.assertEquals(newTaskDTO.description(), createdTask.description());
    }
    */

    @Test
    @DisplayName("delete should run without errors")
    void deleteShouldRunWithoutErrors() {
        TaskDTO taskToDelete = new TaskDTO(
                task1.getId(),
                task1.getDescription(),
                task1.getLength(),
                task1.getProject().getId(),
                new ArrayList<>());

        when(taskRepository.findById(taskToDelete.id())).thenReturn(Optional.of(task1));
        taskService.delete(taskToDelete);
        verify(taskRepository).delete(task1);
    }

    @Test
    @DisplayName("deleteById should throw an exception when ID is invalid")
    void deleteByIdShouldThrowExceptionWhenIDIsInvalid() {
        when(taskRepository.findById(invalidTaskId)).thenReturn(Optional.empty());

        ElementNotFoundException exception = Assertions.assertThrows(ElementNotFoundException.class, () -> {
            taskService.deleteById(invalidTaskId);
        });

        Assertions.assertEquals("Element not found.", exception.getMessage());
    }

    @Test
    @DisplayName("deleteById should run OK when ID is valid")
    void deleteByIdShouldRunOkWhenIDIsValid() {
        when(taskRepository.findById(task1.getId())).thenReturn(Optional.of(task1));

        taskService.deleteById(task1.getId());
        verify(taskRepository).delete(task1);
    }
}
