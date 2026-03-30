package com.codingchallenge.codingchallenge.Service;

import com.codingchallenge.Dto.CreateTaskRequestDto;
import com.codingchallenge.Dto.UpdateTaskRequestDto;
import com.codingchallenge.Entities.Task;
import com.codingchallenge.Enum.TaskStatus;
import com.codingchallenge.Exception.TaskNotFoundException;
import com.codingchallenge.Repository.InMemoryTaskRepository;
import com.codingchallenge.Service.ServiceImpl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private InMemoryTaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = new Task("abc-123", "Write tests", "TDD matters", TaskStatus.PENDING, LocalDate.now().plusDays(5));
    }


    @Test
    void createTask_shouldPersistAndReturnTaskWithGeneratedId() {
        CreateTaskRequestDto request = buildCreateRequest("Deploy service", null, null, LocalDate.now().plusDays(3));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.createTask(request);

        assertThat(result.getId()).isNotBlank();
        assertThat(result.getTitle()).isEqualTo("Deploy service");
        assertThat(result.getStatus()).isEqualTo(TaskStatus.PENDING);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_shouldDefaultStatusToPendingWhenNotProvided() {
        CreateTaskRequestDto request = buildCreateRequest("Review PR", null, null, LocalDate.now().plusDays(1));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.createTask(request);

        assertThat(result.getStatus()).isEqualTo(TaskStatus.PENDING);
    }

    @Test
    void createTask_shouldRespectExplicitStatus() {
        CreateTaskRequestDto request = buildCreateRequest("Fix bug", null, TaskStatus.IN_PROGRESS, LocalDate.now().plusDays(2));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.createTask(request);

        assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }


    @Test
    void getTaskById_shouldReturnTaskWhenFound() {
        when(taskRepository.findById("abc-123")).thenReturn(Optional.of(sampleTask));

        Task result = taskService.getTaskById("abc-123");

        assertThat(result.getId()).isEqualTo("abc-123");
        assertThat(result.getTitle()).isEqualTo("Write tests");
    }

    @Test
    void getTaskById_shouldThrowWhenNotFound() {
        when(taskRepository.findById("ghost-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById("ghost-id"))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("ghost-id");
    }


    @Test
    void updateTask_shouldApplyOnlyProvidedFields() {
        when(taskRepository.findById("abc-123")).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateTaskRequestDto request = new UpdateTaskRequestDto();
        request.setStatus(TaskStatus.DONE);

        Task updated = taskService.updateTask("abc-123", request);

        assertThat(updated.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(updated.getTitle()).isEqualTo("Write tests");
    }

    @Test
    void updateTask_shouldThrowWhenTaskDoesNotExist() {
        when(taskRepository.findById("missing")).thenReturn(Optional.empty());

        UpdateTaskRequestDto request = new UpdateTaskRequestDto();
        request.setTitle("New title");

        assertThatThrownBy(() -> taskService.updateTask("missing", request))
                .isInstanceOf(TaskNotFoundException.class);
    }


    @Test
    void deleteTask_shouldCallRepositoryDeleteWhenTaskExists() {
        when(taskRepository.existsById("abc-123")).thenReturn(true);

        taskService.deleteTask("abc-123");

        verify(taskRepository).deleteById("abc-123");
    }

    @Test
    void deleteTask_shouldThrowWhenTaskDoesNotExist() {
        when(taskRepository.existsById("gone")).thenReturn(false);

        assertThatThrownBy(() -> taskService.deleteTask("gone"))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository, never()).deleteById(any());
    }


    @Test
    void listAllTasks_shouldReturnTasksSortedByDueDate() {
        Task earlier = new Task("1", "First", null, TaskStatus.PENDING, LocalDate.now().plusDays(1));
        Task later = new Task("2", "Second", null, TaskStatus.PENDING, LocalDate.now().plusDays(10));
        when(taskRepository.findAll()).thenReturn(List.of(later, earlier));

        List<Task> result = taskService.listAllTasks(null, 0, 10);

        assertThat(result).extracting(Task::getId).containsExactly("1", "2");
    }

    @Test
    void listAllTasks_shouldFilterByStatusWhenProvided() {
        Task done = new Task("1", "Done task", null, TaskStatus.DONE, LocalDate.now().plusDays(1));
        when(taskRepository.findByStatus(TaskStatus.DONE)).thenReturn(List.of(done));

        List<Task> result = taskService.listAllTasks(TaskStatus.DONE, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    void listAllTasks_shouldReturnEmptyPageWhenOffsetExceedsTotal() {
        Task task = new Task("1", "Only task", null, TaskStatus.PENDING, LocalDate.now().plusDays(1));
        when(taskRepository.findAll()).thenReturn(List.of(task));

        List<Task> result = taskService.listAllTasks(null, 5, 10);

        assertThat(result).isEmpty();
    }


    private CreateTaskRequestDto buildCreateRequest(String title, String description, TaskStatus status, LocalDate dueDate) {
        CreateTaskRequestDto req = new CreateTaskRequestDto();
        req.setTitle(title);
        req.setDescription(description);
        req.setStatus(status);
        req.setDueDate(dueDate);
        return req;
    }
}
