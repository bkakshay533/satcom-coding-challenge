package com.codingchallenge.Repository;

import com.codingchallenge.Entities.Task;
import com.codingchallenge.Enum.TaskStatus;

import java.util.List;
import java.util.Optional;

public interface InMemoryTaskRepository {

    Task save(Task task);

    Optional<Task> findById(String id);

    List<Task> findAll();

    List<Task> findByStatus(TaskStatus status);

    boolean existsById(String id);

    void deleteById(String id);
}
