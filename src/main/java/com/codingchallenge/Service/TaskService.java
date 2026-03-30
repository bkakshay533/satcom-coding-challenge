package com.codingchallenge.Service;

import com.codingchallenge.Dto.CreateTaskRequestDto;
import com.codingchallenge.Dto.UpdateTaskRequestDto;
import com.codingchallenge.Entities.Task;
import com.codingchallenge.Enum.TaskStatus;

import java.util.List;

public interface TaskService {

    Task createTask(CreateTaskRequestDto request);

    Task getTaskById(String id);

    Task updateTask(String id, UpdateTaskRequestDto request);

    void deleteTask(String id);

    List<Task> listAllTasks(TaskStatus statusFilter, int page, int size);
}
