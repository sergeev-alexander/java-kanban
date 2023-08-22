package taskmanager.service;

import org.junit.jupiter.api.Test;
import taskmanager.model.Epic;
import taskmanager.model.Status;
import taskmanager.model.Subtask;
import taskmanager.model.Task;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerTest extends TaskManagerTest<HttpTaskManager> {

    public static DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public HttpTaskManager getManager() {
        return (HttpTaskManager) Managers.getDefaultTaskManager(URI.create("http://localhost:8078/"));
    }

    @Test
    public void getAllTasksTest() {
        Task task = new Task();
        task.setStatus(Status.NEW);
        task.setTitle("Test_title");
        task.setDescription("Test_description");
        task.setStartTime(LocalDateTime.parse("01.01.2023 00:00", DT_FORMATTER));
        task.setDuration(10L);
        manager.addNewTask(task);

        Epic epic = new Epic();
        epic.setStatus(Status.NEW);
        epic.setTitle("Test_title");
        epic.setDescription("Test_description");
        manager.addNewEpic(epic);

        Subtask subtask = new Subtask();
        subtask.setStatus(Status.NEW);
        subtask.setEpicId(2);
        subtask.setTitle("Test_title");
        subtask.setDescription("Test_description");
        subtask.setStartTime(LocalDateTime.parse("01.01.2023 00:10", DT_FORMATTER));
        subtask.setDuration(10L);
        manager.addNewSubtask(subtask);

        TaskManager otherManager = new HttpTaskManager(URI.create("http://localhost:8078/"), true);

        assertEquals(manager.getAllItems(), otherManager.getAllItems());
        assertEquals(manager.getHistory(), otherManager.getHistory());
        assertEquals(manager.getPrioritizedTasks(), otherManager.getPrioritizedTasks());
    }

}
