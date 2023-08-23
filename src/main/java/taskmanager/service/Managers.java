package taskmanager.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import taskmanager.model.AdapterLocalDateTime;

import java.net.URI;
import java.time.LocalDateTime;

public class Managers {

    public static HistoryManager getDefaultHistoryManager() {
        return new InMemoryHistoryManager();
    }

    public static TaskManager getDefaultTaskManager() {
        return new InMemoryTaskManager(getDefaultHistoryManager());
    }

    public static TaskManager getDefaultTaskManager(String backupFileName) {
        return new FileBackedTasksManager(getDefaultHistoryManager(), backupFileName);
    }

    public static TaskManager getDefaultTaskManager(URI url) {
        return new HttpTaskManager(url);
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new AdapterLocalDateTime())
                .setPrettyPrinting()
                .create();
    }

}
