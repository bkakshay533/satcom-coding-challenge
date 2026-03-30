package com.codingchallenge.codingchallenge.Controller;

import com.codingchallenge.Dto.UpdateTaskRequestDto;
import com.codingchallenge.Enum.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void createTask_shouldReturn201WithTaskBody() throws Exception {
        String body = buildCreateJson("Integrate payment gateway", "Stripe integration", null, futureDate(5));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Integrate payment gateway"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.due_date").value(futureDate(5)));
    }

    @Test
    void createTask_shouldReturn400WhenTitleIsMissing() throws Exception {
        String body = buildCreateJson(null, "No title here", null, futureDate(3));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void createTask_shouldReturn400WhenDueDateIsMissing() throws Exception {
        String body = "{\"title\": \"Missing date task\"}";

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTask_shouldReturn400WhenDueDateIsInThePast() throws Exception {
        String body = buildCreateJson("Past task", null, null, LocalDate.now().minusDays(1).toString());

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getTask_shouldReturn200WithTaskWhenFound() throws Exception {
        String taskId = createTaskAndExtractId("Find me", futureDate(4));

        mockMvc.perform(get("/tasks/" + taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Find me"));
    }

    @Test
    void getTask_shouldReturn404ForNonExistentId() throws Exception {
        mockMvc.perform(get("/tasks/definitely-not-real"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }



    @Test
    void updateTask_shouldReturn200WithUpdatedFields() throws Exception {
        String taskId = createTaskAndExtractId("Old title", futureDate(6));

        UpdateTaskRequestDto update = new UpdateTaskRequestDto();
        update.setTitle("New title");
        update.setStatus(TaskStatus.IN_PROGRESS);

        mockMvc.perform(put("/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New title"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void updateTask_shouldReturn404WhenTaskDoesNotExist() throws Exception {
        UpdateTaskRequestDto update = new UpdateTaskRequestDto();
        update.setTitle("Whatever");

        mockMvc.perform(put("/tasks/ghost-task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }



    @Test
    void deleteTask_shouldReturn204OnSuccess() throws Exception {
        String taskId = createTaskAndExtractId("To be deleted", futureDate(7));

        mockMvc.perform(delete("/tasks/" + taskId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/tasks/" + taskId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTask_shouldReturn404WhenTaskDoesNotExist() throws Exception {
        mockMvc.perform(delete("/tasks/phantom"))
                .andExpect(status().isNotFound());
    }



    @Test
    void listTasks_shouldReturnAllTasksSortedByDueDate() throws Exception {
        createTaskAndExtractId("Later task", futureDate(10));
        createTaskAndExtractId("Earlier task", futureDate(2));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Earlier task"))
                .andExpect(jsonPath("$[1].title").value("Later task"));
    }

    @Test
    void listTasks_shouldFilterByStatusWhenProvided() throws Exception {
        String id = createTaskAndExtractId("Done task", futureDate(3));
        createTaskAndExtractId("Pending task", futureDate(4));

        UpdateTaskRequestDto update = new UpdateTaskRequestDto();
        update.setStatus(TaskStatus.DONE);
        mockMvc.perform(put("/tasks/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)));

        mockMvc.perform(get("/tasks?status=DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("DONE"));
    }

    @Test
    void listTasks_shouldSupportPagination() throws Exception {
        for (int i = 1; i <= 5; i++) {
            createTaskAndExtractId("Task " + i, futureDate(i));
        }

        mockMvc.perform(get("/tasks?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/tasks?page=2&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }


    private String buildCreateJson(String title, String description, TaskStatus status, String dueDate) throws Exception {
        StringBuilder json = new StringBuilder("{");
        if (title != null) json.append("\"title\":\"").append(title).append("\",");
        if (description != null) json.append("\"description\":\"").append(description).append("\",");
        if (status != null) json.append("\"status\":\"").append(status).append("\",");
        if (dueDate != null) json.append("\"due_date\":\"").append(dueDate).append("\"");
        String result = json.toString().replaceAll(",+$", "");
        return result + "}";
    }

    private String createTaskAndExtractId(String title, String dueDate) throws Exception {
        String body = buildCreateJson(title, null, null, dueDate);

        MvcResult result = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseJson).get("id").asText();
    }

    private String futureDate(int daysFromNow) {
        return LocalDate.now().plusDays(daysFromNow).toString();
    }
}