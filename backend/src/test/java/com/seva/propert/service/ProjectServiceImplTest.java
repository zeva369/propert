package com.seva.propert.service;

import com.seva.propert.exception.DuplicatedElementException;
import com.seva.propert.exception.ElementNotFoundException;
import com.seva.propert.model.entity.Project;
import com.seva.propert.repository.ProjectRepository;
import com.seva.propert.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {
    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Test
    @DisplayName("findAll should return an empty list")
    void findAllShouldReturnAnEmptyList() {
        // Mock the behavior of projectRepository.findAll() to return an empty list
        when(projectRepository.findAll()).thenReturn(Collections.emptyList());

        List<Project> result = projectService.findAll();
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findAll should return a list of projects")
    void findAllShouldReturnAListOfProjects() {
        // Mock the behavior of projectRepository.findAll() to return a list with one project
        Project project = new Project();
        project.setId(1L);
        project.setName("Test Project");
        when(projectRepository.findAll()).thenReturn(Collections.singletonList(project));

        List<Project> result = projectService.findAll();
        assertEquals(1, result.size());
        assertEquals("Test Project", result.get(0).getName());
        assertEquals("Test Project", result.get(0).getName());
    }

    @Test
    @DisplayName("findById should return an Empty Optional")
    void findByIdShouldReturnEmpty() {
        // Mock the behavior of projectRepository.findById() to return an empty Optional
        when(projectRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        Optional<Project> result = projectService.findById(1L);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findById should return a valid Project")
    void findByIdShouldReturnAValidProject() {
        // Mock the behavior of projectRepository.findById() to return a non-empty Optional
        Project project = new Project();
        project.setId(1L);
        project.setName("Test Project");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        Optional<Project> result = projectService.findById(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Test Project", result.get().getName());
    }

    @Test
    @DisplayName("findByUserId should return an Empty List")
    void findByUserIdShouldReturnAnEmptyList() {
        String invalidUsername = "invalidUser";

        // Mock the behavior of projectRepository.findByUserId() to return an empty list
        when(projectRepository.findByUserId(invalidUsername)).thenReturn(Collections.emptyList());

        List<Project> result = projectService.findByUserId(invalidUsername);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByUserId should return a List of Projects")
    void findByUserIdShouldReturnAListOfProjects() {
        String username = "testUser";

        // Mock the behavior of projectRepository.findByUserId() to return a list with one project
        Project project = new Project();
        project.setId(1L);
        project.setName("Test Project");
        when(projectRepository.findByUserId(username)).thenReturn(Collections.singletonList(project));

        List<Project> result = projectService.findByUserId(username);
        assertEquals(1, result.size());
        assertEquals("Test Project", result.get(0).getName());
        assertEquals("Test Project", result.get(0).getName());
    }

    @Test
    @DisplayName("findByNameContaining should return an empty list")
    void findByNameContainingShouldReturnAnEmptyList() {
        String pattern = "nonexistent";

        // Mock the behavior of projectRepository.findByNameContaining() to return an empty list
        when(projectRepository.findByNameContaining(pattern)).thenReturn(Collections.emptyList());

        List<Project> result = projectService.findByNameContaining(pattern);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByNameContaining should return a list of projects")
    void findByNameContainingShouldReturnAListOfProjects() {
        String pattern = "Test";

        // Mock the behavior of projectRepository.findByNameContaining() to return a list with one project
        Project project = new Project();
        project.setId(1L);
        project.setName("Test Project");
        when(projectRepository.findByNameContaining(pattern)).thenReturn(Collections.singletonList(project));

        List<Project> result = projectService.findByNameContaining(pattern);
        assertEquals(1, result.size());
        assertEquals("Test Project", result.get(0).getName());
    }

    @Test
    @DisplayName("save should return a saved project")
    void saveShouldReturnASavedProject() {
        Project project = new Project();
        project.setId(1L);
        project.setName("Test Project");

        // Mock the behavior of projectRepository.save() to return the same project
        when(projectRepository.save(project)).thenReturn(project);

        Project result = projectService.save(project);
        assertEquals(1L, result.getId());
        assertEquals("Test Project", result.getName());
    }

    @Test
    @DisplayName("delete should run without exceptions")
    void deleteShouldRunWithoutExceptions() {
        Project project = new Project();
        project.setId(1L);
        project.setName("Test Project");

        projectService.delete(project);
        verify(projectRepository).delete(project); // Verify that delete was called with the correct project
    }

    @Test
    @DisplayName("create should return DuplicatedElementException if project already exists")
    void createShouldReturnDuplicatedElementException() {
        Project project = new Project();
        project.setId(1L);
        project.setName("Test Project");

        // Mock the behavior of projectRepository.findById() to return a list with the same project
        when(projectRepository.findById(project.getId()))
                .thenReturn(Optional.of(project));

        DuplicatedElementException exception = Assertions.assertThrows(
                DuplicatedElementException.class,
                () -> projectService.create(project)
        );

        Assertions.assertEquals("Duplicated element.", exception.getMessage());
    }

    @Test
    @DisplayName("create should return a saved project")
    void createShouldReturnASavedProject() {
        Project project = new Project();
        project.setId(1L);
        project.setName("Test Project");

        // Mock the behavior of projectRepository.findById() to return an empty Optional
        when(projectRepository.findById(project.getId()))
                .thenReturn(Optional.empty());

        // Mock the behavior of projectRepository.save() to return the same project
        when(projectRepository.save(project)).thenReturn(project);

        Project result = projectService.create(project);
        assertEquals(1L, result.getId());
        assertEquals("Test Project", result.getName());
    }

    @Test
    @DisplayName("deleteById should raise an exception if the project is not found")
    void deleteByIdShouldRaiseAnExceptionIfTheProjectIsNotFound() {
        Long projectId = 1L;

        // Mock the behavior of projectRepository.findById() to return an empty Optional
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Call the method and assert that it throws an exception
        ElementNotFoundException exception = Assertions.assertThrows(
                ElementNotFoundException.class,
                () -> projectService.deleteById(projectId)
        );

        Assertions.assertEquals("Element not found.", exception.getMessage());
    }

    @Test
    @DisplayName("deleteById should delete the project if it exists")
    void deleteByIdShouldDeleteTheProjectIfItExists() {
        Long projectId = 1L;
        Project project = new Project();
        project.setId(projectId);
        project.setName("Test Project");

        // Mock the behavior of projectRepository.findById() to return the project
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // Call the method
        projectService.deleteById(projectId);

        // Verify that the delete method was called with the correct project
        verify(projectRepository).delete(project);
    }
}