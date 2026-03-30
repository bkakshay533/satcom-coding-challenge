package com.codingchallenge.codingchallenge.Repository;


import com.codingchallenge.Entities.Task;
import com.codingchallenge.Enum.TaskStatus;
import com.codingchallenge.Repository.InMemoryTaskRepository;
import com.codingchallenge.Repository.RepositoryImpl.InMemoryTaskRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryTaskRepositoryTest {

    private InMemoryTaskRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTaskRepositoryImpl();
    }

    @Test
    void save_shouldStoreAndReturnTask() {
        Task task = makeTask("id-1", TaskStatus.PENDING);
        Task saved = repository.save(task);

        assertThat(saved.getId()).isEqualTo("id-1");
    }

    @Test
    void findById_shouldReturnTaskAfterSave() {
        repository.save(makeTask("id-2", TaskStatus.IN_PROGRESS));

        Optional<Task> found = repository.findById("id-2");

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void findById_shouldReturnEmptyForUnknownId() {
        Optional<Task> found = repository.findById("does-not-exist");
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllSavedTasks() {
        repository.save(makeTask("a", TaskStatus.PENDING));
        repository.save(makeTask("b", TaskStatus.DONE));

        List<Task> all = repository.findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    void findByStatus_shouldReturnOnlyMatchingTasks() {
        repository.save(makeTask("x", TaskStatus.PENDING));
        repository.save(makeTask("y", TaskStatus.DONE));
        repository.save(makeTask("z", TaskStatus.DONE));

        List<Task> done = repository.findByStatus(TaskStatus.DONE);

        assertThat(done).hasSize(2);
        assertThat(done).allMatch(t -> t.getStatus() == TaskStatus.DONE);
    }

    @Test
    void existsById_shouldReturnTrueAfterSave() {
        repository.save(makeTask("id-3", TaskStatus.PENDING));
        assertThat(repository.existsById("id-3")).isTrue();
    }

    @Test
    void existsById_shouldReturnFalseForMissingTask() {
        assertThat(repository.existsById("phantom")).isFalse();
    }

    @Test
    void deleteById_shouldRemoveTaskFromStore() {
        repository.save(makeTask("id-4", TaskStatus.PENDING));
        repository.deleteById("id-4");

        assertThat(repository.existsById("id-4")).isFalse();
        assertThat(repository.findById("id-4")).isEmpty();
    }

    @Test
    void save_shouldOverwriteExistingTaskWithSameId() {
        Task original = makeTask("id-5", TaskStatus.PENDING);
        repository.save(original);

        original.setStatus(TaskStatus.DONE);
        repository.save(original);

        assertThat(repository.findById("id-5").get().getStatus()).isEqualTo(TaskStatus.DONE);
    }

    private Task makeTask(String id, TaskStatus status) {
        return new Task(id, "Task " + id, "desc", status, LocalDate.now().plusDays(7));
    }
}
