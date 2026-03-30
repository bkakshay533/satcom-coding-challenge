package com.codingchallenge.Controller;

import com.codingchallenge.Dto.CreateTaskRequestDto;
import com.codingchallenge.Dto.TaskResponseDto;
import com.codingchallenge.Dto.UpdateTaskRequestDto;
import com.codingchallenge.Entities.Task;
import com.codingchallenge.Enum.TaskStatus;
import com.codingchallenge.Service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(@Valid @RequestBody CreateTaskRequestDto request) {
        Task created = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponseDto.from(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTask(@PathVariable String id) {
        Task task = taskService.getTaskById(id);
        return ResponseEntity.ok(TaskResponseDto.from(task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(
            @PathVariable String id,
            @Valid @RequestBody UpdateTaskRequestDto request) {
        Task updated = taskService.updateTask(id, request);
        return ResponseEntity.ok(TaskResponseDto.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> listTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<Task> tasks = taskService.listAllTasks(status, page, size);
        List<TaskResponseDto> responseBody = tasks.stream()
                .map(TaskResponseDto::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseBody);
    }
}