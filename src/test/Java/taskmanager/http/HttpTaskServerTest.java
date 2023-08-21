package taskmanager.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanager.model.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpTaskServerTest {

    static HttpTaskServer server;
    HttpClient client = HttpClient.newHttpClient();
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new AdapterLocalDateTime())
            .setPrettyPrinting()
            .create();

    public static DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @BeforeAll
    public static void startServer() throws IOException {
        server = new HttpTaskServer();
        server.start();
    }

    @BeforeEach
    public void deleteAllTasks() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @AfterAll
    public static void stopServer() {
        server.stop();
    }

    @Test
    public void getAllTasksTest() throws IOException, InterruptedException {
        Task task = createAndPostTestingTask();
        Epic epic = createAndPostTestingEpic();
        Subtask subtask = createAndPostTestingSubtask();
        task.setId(1);
        epic.setId(2);
        epic.setSubTasksIdList(List.of(3));
        epic.setStartTime(LocalDateTime.parse("01.01.2023 00:10", DT_FORMATTER));
        epic.setDuration(10);
        epic.setEndTime(LocalDateTime.parse("01.01.2023 00:20", DT_FORMATTER));
        subtask.setId(3);
        List<Task> taskList = List.of(task, epic, subtask);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(GSON.toJson(taskList), response.body());
    }

    @Test
    public void deleteAllTasksTest() throws IOException, InterruptedException {
        createAndPostTestingTask();
        createAndPostTestingEpic();
        createAndPostTestingSubtask();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(response.body(), "All items where deleted!");
    }

    @Test
    public void getHistoryTest() throws IOException, InterruptedException {
        createAndPostTestingTask();
        createAndPostTestingEpic();
        createAndPostTestingSubtask();

        URI url1 = URI.create("http://localhost:8080/tasks/task/?id=1");
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url1)
                .GET()
                .build();
        client.send(request1, HttpResponse.BodyHandlers.ofString());

        URI url2 = URI.create("http://localhost:8080/tasks/epic/?id=2");
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(url2)
                .GET()
                .build();
        client.send(request2, HttpResponse.BodyHandlers.ofString());

        URI url3 = URI.create("http://localhost:8080/tasks/subtask/?id=3");
        HttpRequest request3 = HttpRequest.newBuilder()
                .uri(url3)
                .GET()
                .build();
        client.send(request3, HttpResponse.BodyHandlers.ofString());

        URI url = URI.create("http://localhost:8080/tasks/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        List<Task> taskList = GSON.fromJson(response.body(),
                new TypeToken<List<Task>>() {
                }.getType());
        List<Integer> historyIdList = List.of(taskList.get(0).getId(), taskList.get(1).getId(), taskList.get(2).getId());
        List<Integer> expectedIdList = List.of(3, 2, 1);
        assertEquals(expectedIdList, historyIdList);
    }

    @Test
    public void getPriorityTest() throws IOException, InterruptedException {
        createAndPostTestingTask();
        createAndPostTestingEpic();
        createAndPostTestingSubtask();

        URI url = URI.create("http://localhost:8080/tasks/priority");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        List<Task> taskList = GSON.fromJson(response.body(),
                new TypeToken<List<Task>>() {
                }.getType());
        List<Integer> historyIdList = List.of(taskList.get(0).getId(), taskList.get(1).getId());
        List<Integer> expectedIdList = List.of(1, 3);
        assertEquals(expectedIdList, historyIdList);
    }

    @Test
    public void postAndGetTaskEpicSubtaskTest() throws IOException, InterruptedException {
        Task task = createAndPostTestingTask();
        Epic epic = createAndPostTestingEpic();
        Subtask subtask = createAndPostTestingSubtask();
        task.setId(1);
        epic.setId(2);
        epic.setSubTasksIdList(List.of(3));
        epic.setStartTime(LocalDateTime.parse("01.01.2023 00:10", DT_FORMATTER));
        epic.setDuration(10);
        epic.setEndTime(LocalDateTime.parse("01.01.2023 00:20", DT_FORMATTER));
        subtask.setId(3);

        URI url1 = URI.create("http://localhost:8080/tasks/task/?id=1");
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url1)
                .GET()
                .build();
        HttpResponse<String> responseTaskStr = client.send(request1, HttpResponse.BodyHandlers.ofString());

        URI url2 = URI.create("http://localhost:8080/tasks/epic/?id=2");
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(url2)
                .GET()
                .build();
        HttpResponse<String> responseEpicStr = client.send(request2, HttpResponse.BodyHandlers.ofString());

        URI url3 = URI.create("http://localhost:8080/tasks/subtask/?id=3");
        HttpRequest request3 = HttpRequest.newBuilder()
                .uri(url3)
                .GET()
                .build();
        HttpResponse<String> responseSubtaskStr = client.send(request3, HttpResponse.BodyHandlers.ofString());

        assertEquals(GSON.toJson(task), responseTaskStr.body());
        assertEquals(GSON.toJson(epic), responseEpicStr.body());
        assertEquals(GSON.toJson(subtask), responseSubtaskStr.body());
    }

    @Test
    public void deleteByIdTest() throws IOException, InterruptedException {
        createAndPostTestingTask();
        createAndPostTestingEpic();
        createAndPostTestingSubtask();

        URI url1 = URI.create("http://localhost:8080/tasks/task/?id=1");
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url1)
                .DELETE()
                .build();
        client.send(request1, HttpResponse.BodyHandlers.ofString());

        URI url3 = URI.create("http://localhost:8080/tasks/subtask/?id=3");
        HttpRequest request3 = HttpRequest.newBuilder()
                .uri(url3)
                .DELETE()
                .build();
        client.send(request3, HttpResponse.BodyHandlers.ofString());

        URI url2 = URI.create("http://localhost:8080/tasks/epic/?id=2");
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(url2)
                .DELETE()
                .build();
        client.send(request2, HttpResponse.BodyHandlers.ofString());

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals("[]", response.body());
    }


    private Task createAndPostTestingTask() throws IOException, InterruptedException {
        Task task = new Task();
        task.setStatus(Status.NEW);
        task.setTitle("Test_title");
        task.setDescription("Test_description");
        task.setStartTime(LocalDateTime.parse("01.01.2023 00:00", DT_FORMATTER));
        task.setDuration(10L);

        String jsonTask = GSON.toJson(task);
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(jsonTask);
        URI url = URI.create("http://localhost:8080/tasks/task/?id=1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(body)
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
        return task;
    }

    private Epic createAndPostTestingEpic() throws IOException, InterruptedException {
        Epic epic = new Epic();
        epic.setStatus(Status.NEW);
        epic.setTitle("Test_title");
        epic.setDescription("Test_description");

        String jsonEpic = GSON.toJson(epic);
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(jsonEpic);
        URI url = URI.create("http://localhost:8080/tasks/epic/?id=2");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(body)
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
        return epic;
    }

    private Subtask createAndPostTestingSubtask() throws IOException, InterruptedException {
        Subtask subtask = new Subtask();
        subtask.setStatus(Status.NEW);
        subtask.setEpicId(2);
        subtask.setTitle("Test_title");
        subtask.setDescription("Test_description");
        subtask.setStartTime(LocalDateTime.parse("01.01.2023 00:10", DT_FORMATTER));
        subtask.setDuration(10L);

        String jsonSubtask = GSON.toJson(subtask);
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(jsonSubtask);
        URI url = URI.create("http://localhost:8080/tasks/subtask/?id=3");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(body)
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
        return subtask;
    }
}



