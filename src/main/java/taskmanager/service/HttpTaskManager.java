package taskmanager.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import taskmanager.http.KVTaskClient;
import taskmanager.model.Epic;
import taskmanager.model.Subtask;
import taskmanager.model.Task;

import java.net.URI;
import java.util.stream.Collectors;

public class HttpTaskManager extends FileBackedTasksManager {

    private static final String TASK_KEY = "tasks/";
    private static final String EPIC_KEY = "epics/";
    private static final String SUBTASK_KEY = "subtasks/";
    private static final String HISTORY_KEY = "history/";
    private final KVTaskClient client;
    private static final Gson GSON = Managers.getGson();

    public HttpTaskManager(URI url, boolean needToBackup) {
        client = new KVTaskClient(url.toString());
        if (needToBackup) {
            backup();
        }
    }

    public HttpTaskManager(URI url) {
        this(url, false);
    }

    @Override
    protected void save() {
        String jsonTasks = GSON.toJson(taskMap.values());
        String jsonEpics = GSON.toJson(epicMap.values());
        String jsonSubtasks = GSON.toJson(subtaskMap.values());
        String jsonHistory = GSON.toJson(getHistory().stream()
                .map(Task::getId)
                .collect(Collectors.toList()));

        client.put(TASK_KEY, jsonTasks);
        client.put(EPIC_KEY, jsonEpics);
        client.put(SUBTASK_KEY, jsonSubtasks);
        client.put(HISTORY_KEY, jsonHistory);
    }

    private void backup() {
        JsonElement jsonTasks = JsonParser.parseString(client.load(TASK_KEY));
        if (!jsonTasks.isJsonNull()) {
            JsonArray jsonTasksArray = jsonTasks.getAsJsonArray();
            for (JsonElement jsonTask : jsonTasksArray) {
                Task task = GSON.fromJson(jsonTask, Task.class);
                taskMap.put(task.getId(), task);
                prioritySet.add(task);
            }
        }
        JsonElement jsonEpics = JsonParser.parseString(client.load(EPIC_KEY));
        if (!jsonEpics.isJsonNull()) {
            JsonArray jsonEpicsArray = jsonEpics.getAsJsonArray();
            for (JsonElement jsonEpic : jsonEpicsArray) {
                Epic epic = GSON.fromJson(jsonEpic, Epic.class);
                epicMap.put(epic.getId(), epic);
            }
        }
        JsonElement jsonSubtasks = JsonParser.parseString(client.load(SUBTASK_KEY));
        if (!jsonSubtasks.isJsonNull()) {
            JsonArray jsonSubtasksArray = jsonSubtasks.getAsJsonArray();
            for (JsonElement jsonSubtask : jsonSubtasksArray) {
                Subtask subtask = GSON.fromJson(jsonSubtask, Subtask.class);
                subtaskMap.put(subtask.getId(), subtask);
                prioritySet.add(subtask);
            }
        }
        JsonElement jsonHistory = JsonParser.parseString(client.load(HISTORY_KEY));
        if (!jsonHistory.isJsonNull()) {
            JsonArray jsonHistoryArray = jsonHistory.getAsJsonArray();
            for (JsonElement jsonHistoryId : jsonHistoryArray) {
                int id = jsonHistoryId.getAsInt();
                getTaskById(id);
            }
        }
    }

}
