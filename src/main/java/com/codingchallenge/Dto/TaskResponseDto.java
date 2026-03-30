package com.codingchallenge.Dto;


import com.codingchallenge.Entities.Task;
import com.codingchallenge.Enum.TaskStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;


import java.time.LocalDate;


public class TaskResponseDto {

    private String id;
    private String title;
    private String description;
    private TaskStatus status;

    @JsonProperty("due_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    public static TaskResponseDto from(Task task) {
        TaskResponseDto response = new TaskResponseDto();
        response.id = task.getId();
        response.title = task.getTitle();
        response.description = task.getDescription();
        response.status = task.getStatus();
        response.dueDate = task.getDueDate();
        return response;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }
}