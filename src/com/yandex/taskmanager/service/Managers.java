package com.yandex.taskmanager.service;

import java.io.File;

public class Managers {

    public static HistoryManager getDefaultHistoryManager() {
        return new InMemoryHistoryManager();
    }

    public static TaskManager getDefaultTaskManager() {
        return new InMemoryTaskManager(getDefaultHistoryManager());
    }

    public static TaskManager getDefaultTaskManager(File backupFile) {
        return new FileBackedTasksManager(getDefaultHistoryManager(), backupFile);
    }

}
