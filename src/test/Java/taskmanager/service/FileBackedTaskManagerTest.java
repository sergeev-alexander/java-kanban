package taskmanager.service;

import org.junit.jupiter.api.Test;
import taskmanager.model.Epic;
import taskmanager.model.Subtask;
import taskmanager.model.Task;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTasksManager> {

    @Override
    public FileBackedTasksManager getManager() {
        return (FileBackedTasksManager) Managers.getDefaultTaskManager(new File("Test_backup_file.csv"));
    }

    @Test
    public void whenTryToLoadFromFileAllLoadedShouldBeEqualToCurrentManager() {
        FileBackedTasksManager manager = getManager();
        List<Task> addingTasks = createSomeTasks();
        manager.addNewTask(addingTasks.get(0));
        manager.addNewEpic((Epic) addingTasks.get(1));
        manager.addNewSubtask((Subtask) addingTasks.get(2));
        manager.getTaskById(1);
        manager.getTaskById(2);
        manager.getTaskById(3);

        TaskManager loadedManager = FileBackedTasksManager.loadFromFile(new File("Test_backup_file.csv"));

        assertEquals(manager.getAllItems(), loadedManager.getAllItems(), "All items are not equal to loaded items!");
        assertEquals(manager.getHistory(), loadedManager.getHistory(), "History is not equal to loaded history!");
        assertEquals(manager.getPrioritizedTasks(), loadedManager.getPrioritizedTasks(),
                "Prioritized list is not equal to loaded prioritized list!");
        assertEquals(manager.getAllTasks(), loadedManager.getAllTasks(), "All tasks are not equal to loaded tasks!");
        assertEquals(manager.getAllEpics(), loadedManager.getAllEpics(), "All epics are not equal to loaded epics!");
        assertEquals(manager.getAllSubtasks(), loadedManager.getAllSubtasks(),
                "All subtasks are not equal to loaded subtasks!");

    }

    @Test
    public void whenTryToLoadFromFileIdFieldShouldBeEqualToCurrentManager() {
        FileBackedTasksManager manager = getManager();
        List<Task> addingTasks = createSomeTasks();
        manager.addNewTask(addingTasks.get(0));
        manager.addNewEpic((Epic) addingTasks.get(1));
        manager.addNewSubtask((Subtask) addingTasks.get(2));
        manager.getTaskById(1);
        manager.getTaskById(2);
        manager.getTaskById(3);
        TaskManager loadedManager = FileBackedTasksManager.loadFromFile(new File("Test_backup_file.csv"));
        assertEquals(manager.getIdField(), loadedManager.getIdField(), "Id field is not equal to loaded id field!");
    }
}
