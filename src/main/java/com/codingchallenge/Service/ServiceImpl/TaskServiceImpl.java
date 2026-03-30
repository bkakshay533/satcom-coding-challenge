package com.codingchallenge.Service.ServiceImpl;

import com.codingchallenge.Dto.CreateTaskRequestDto;
import com.codingchallenge.Dto.UpdateTaskRequestDto;
import com.codingchallenge.Entities.Task;
import com.codingchallenge.Enum.TaskStatus;
import com.codingchallenge.Exception.TaskNotFoundException;
import com.codingchallenge.Repository.InMemoryTaskRepository;

import com.codingchallenge.Service.TaskService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    private final InMemoryTaskRepository taskRepository;

    public TaskServiceImpl(InMemoryTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public Task createTask(CreateTaskRequestDto request) {
        String id = UUID.randomUUID().toString();
        TaskStatus status = request.getStatus() != null ? request.getStatus() : TaskStatus.PENDING;

        Task newTask = new Task(id, request.getTitle(), request.getDescription(), status, request.getDueDate());
        return taskRepository.save(newTask);
    }

    @Override
    public Task getTaskById(String id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("No task found with id: " + id));
    }

    @Override
    public Task updateTask(String id, UpdateTaskRequestDto request) {
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("No task found with id: " + id));

        if (request.getTitle() != null) {
            existing.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        if (request.getDueDate() != null) {
            existing.setDueDate(request.getDueDate());
        }

        return taskRepository.save(existing);
    }

    @Override
    public void deleteTask(String id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException("No task found with id: " + id);
        }
        taskRepository.deleteById(id);
    }

    @Override
    public List<Task> listAllTasks(TaskStatus statusFilter, int page, int size) {
        List<Task> tasks = (statusFilter != null)
                ? taskRepository.findByStatus(statusFilter)
                : taskRepository.findAll();

        List<Task> sorted = tasks.stream()
                .sorted(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        int fromIndex = page * size;
        if (fromIndex >= sorted.size()) {
            return List.of();
        }
        int toIndex = Math.min(fromIndex + size, sorted.size());
        return sorted.subList(fromIndex, toIndex);
    }
}
