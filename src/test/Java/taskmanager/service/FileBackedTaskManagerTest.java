package taskmanager.service;

import java.io.File;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTasksManager> {

    @Override
    public FileBackedTasksManager getManager() {
        return (FileBackedTasksManager) Managers.getDefaultTaskManager(new File("Test_backup_file.csv"));
    }
}
