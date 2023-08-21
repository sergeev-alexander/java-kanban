package taskmanager.service;

import java.io.IOException;
import java.net.URI;

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

    public static TaskManager getDefaultTaskManager(URI url) throws IOException, InterruptedException {
        return new HttpTaskManager(url);
    }

}
