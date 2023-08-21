package taskmanager.service;

import com.google.gson.*;
import taskmanager.http.KVServer;
import taskmanager.http.KVTaskClient;
import taskmanager.model.AdapterLocalDateTime;
import taskmanager.model.Epic;
import taskmanager.model.Subtask;
import taskmanager.model.Task;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;

public class HttpTaskManager extends FileBackedTasksManager {

    private final KVTaskClient client;
    private final KVServer server;
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new AdapterLocalDateTime())
            .setPrettyPrinting()
            .create();

    public HttpTaskManager(URI url) throws IOException, InterruptedException {
        server = new KVServer();
        server.start();
        client = new KVTaskClient(url.toString());
        backup();
    }

    @Override
    public void stopServer(){
        server.stopKVServer();
    }

    @Override
    protected void save() {
        String jsonTasks = GSON.toJson(taskMap.values());
        String jsonEpics = GSON.toJson(epicMap.values());
        String jsonSubtasks = GSON.toJson(subtaskMap.values());
        String jsonHistory = GSON.toJson(getHistory());

        client.put("tasks/", jsonTasks);
        client.put("epics/", jsonEpics);
        client.put("subtasks/", jsonSubtasks);
        client.put("history/", jsonHistory);
    }

    public void backup() {
        JsonElement jsonTasks = JsonParser.parseString(client.load("tasks"));
        if (!jsonTasks.isJsonNull()) {
            JsonArray jsonTasksArray = jsonTasks.getAsJsonArray();
            for (JsonElement jsonTask : jsonTasksArray) {
                Task task = GSON.fromJson(jsonTask, Task.class);
                taskMap.put(task.getId(), task);
                prioritySet.add(task);
            }
        }
        JsonElement jsonEpics = JsonParser.parseString(client.load("epics"));
        if (!jsonEpics.isJsonNull()) {
            JsonArray jsonEpicsArray = jsonEpics.getAsJsonArray();
            for (JsonElement jsonEpic : jsonEpicsArray) {
                Epic epic = GSON.fromJson(jsonEpic, Epic.class);
                epicMap.put(epic.getId(), epic);
                prioritySet.add(epic);
            }
        }
        JsonElement jsonSubtasks = JsonParser.parseString(client.load("subtasks"));
        if (!jsonSubtasks.isJsonNull()) {
            JsonArray jsonSubtasksArray = jsonSubtasks.getAsJsonArray();
            for (JsonElement jsonSubtask : jsonSubtasksArray) {
                Subtask subtask = GSON.fromJson(jsonSubtask, Subtask.class);
                taskMap.put(subtask.getId(), subtask);
                prioritySet.add(subtask);
            }
        }
        JsonElement jsonHistory = JsonParser.parseString(client.load("history"));
        if (!jsonHistory.isJsonNull()) {
            JsonArray jsonHistoryArray = jsonHistory.getAsJsonArray();
            for (JsonElement jsonHistoryElement : jsonHistoryArray) {
                Task task = GSON.fromJson(jsonHistoryElement, Task.class);
                getTaskById(task.getId());
            }
        }
    }

}
