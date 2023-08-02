package taskmanager.service;

import taskmanager.exceptions.AddingAndUpdatingException;
import taskmanager.exceptions.NoSuchTaskException;

import java.io.File;

public class Managers {

    public static HistoryManager getDefaultHistoryManager() {
        return new InMemoryHistoryManager();
    }

    public static TaskManager getDefaultTaskManager() {
        return new InMemoryTaskManager(getDefaultHistoryManager());
    }

    public static TaskManager getDefaultTaskManager(File backupFile) throws NoSuchTaskException, AddingAndUpdatingException {
        return new FileBackedTasksManager(getDefaultHistoryManager(), backupFile);
    }

}
