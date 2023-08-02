package taskmanager.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanager.exceptions.AddingAndUpdatingException;
import taskmanager.exceptions.NoSuchTaskException;
import taskmanager.model.*;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TaskManagerTest {

    TaskManager manager = Managers.getDefaultTaskManager();

    public static DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    public static ZoneId ZONE_ID = ZoneId.of("Europe/Moscow");

    @BeforeEach
    void setUp() {
        manager.deleteAllItems();
    }

    @AfterEach
    void tearDown() {
        manager.deleteAllItems();
    }

    /**
     * Получение истрии просмотров со стандартным поведением
     */
    @Test
    void getHistory() {
        Task task1 = new Task();
        Epic epic2 = new Epic();
        Subtask subtask3 = new Subtask();
        subtask3.setEpicId(2);
        List<Task> controlTaskList = List.of(epic2, subtask3, task1);
        try {
            manager.addNewTask(task1);
            manager.addNewEpic(epic2);
            manager.addNewSubtask(subtask3);
            manager.getTaskById(1);
            manager.getTaskById(3);
            manager.getTaskById(2);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        List<Task> receivedTaskList = manager.getHistory();
        assertEquals(controlTaskList, receivedTaskList, "Received history is wrong or absent!");
    }

    /**
     * Получение истории в отсутствие просмотров
     */
    @Test
    void whenTryToGetHistoryOfViewsInTheirAbsenceShouldReceiveAnEmptyList() {
        Task task1 = new Task();
        Epic epic2 = new Epic();
        Subtask subtask3 = new Subtask();
        subtask3.setEpicId(2);
        try {
            manager.addNewTask(task1);
            manager.addNewEpic(epic2);
            manager.addNewSubtask(subtask3);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        List<Task> receivedHistoryList = manager.getHistory();
        assertTrue(receivedHistoryList.isEmpty(), "Received not empty history list");
    }

    /**
     * Получение истории просмотров и задач из backup файла со стандарным поведением.
     */
    @Test
    void whenUseLoadingFromFileHistoryListShouldBeLoadedCorrectly() {
        Task task1 = new Task();
        Epic epic2 = new Epic();
        Subtask subtask3 = new Subtask();
        subtask3.setEpicId(2);
        List<Task> controlHistoryList = null;
        try {
            TaskManager fileBackedTasksManager =
                    Managers.getDefaultTaskManager(new File("Test_backup_file.csv"));
            fileBackedTasksManager.deleteAllItems();
            fileBackedTasksManager.addNewTask(task1);
            fileBackedTasksManager.addNewEpic(epic2);
            fileBackedTasksManager.addNewSubtask(subtask3);
            fileBackedTasksManager.getTaskById(1);
            fileBackedTasksManager.getTaskById(3);
            fileBackedTasksManager.getTaskById(2);
            controlHistoryList = fileBackedTasksManager.getHistory();
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        manager.deleteAllItems();
        List<Task> mustBeEmptyTaskList = manager.getAllTasks();
        List<Task> mustBeEmptyHistoryList = manager.getHistory();
        assertTrue(mustBeEmptyHistoryList.isEmpty(), "History list is not empty!");
        assertTrue(mustBeEmptyTaskList.isEmpty(), "Task list is not empty!");
        List<Task> receivedFromBackupHistoryList = null;
        try {
            FileBackedTasksManager anotherFileBackedTasksManager =
                    FileBackedTasksManager.loadFromFile(new File("Test_backup_file.csv"));
            receivedFromBackupHistoryList = anotherFileBackedTasksManager.getHistory();
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        assert receivedFromBackupHistoryList != null;
        assert controlHistoryList != null;
        assertEquals(controlHistoryList.get(0).getId(), receivedFromBackupHistoryList.get(0).getId(),
                "Loaded history list is wrong or absent!");
        assertEquals(controlHistoryList.get(1).getId(), receivedFromBackupHistoryList.get(1).getId(),
                "Loaded history list is wrong or absent!");
        assertEquals(controlHistoryList.get(2).getId(), receivedFromBackupHistoryList.get(2).getId(),
                "Loaded history list is wrong or absent!");

    }

    /**
     * Получение истории просмотров и задач из backup файла с пустым файлом.
     */
    @Test
    void whenUseLoadingFromFileHistoryInItsAbsenceHistoryListShouldBeEmpty() {
        List<Task> receivedHistoryList = null;
        try {
            TaskManager fileBackedTaskManager =
                    Managers.getDefaultTaskManager(new File("Test_backup_file.csv"));
            fileBackedTaskManager.deleteAllItems();
            TaskManager anotherFileBackedTaskManager =
                    FileBackedTasksManager.loadFromFile(new File("Test_backup_file.csv"));
            receivedHistoryList = anotherFileBackedTaskManager.getHistory();
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        assert receivedHistoryList != null;
        assertTrue(receivedHistoryList.isEmpty());
    }

    /**
     * Получение задачи из backup файла со стандарным поведением
     */
    @Test
    void whenUseLoadingFromFileAllTaskFieldsShouldBeLoadedCorrectly() {
        Task task = new Task();
        task.setTitle("Test_Title");
        task.setDescription("Test_Description");
        task.setStatus(Status.IN_PROGRESS);
        task.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:00", DT_FORMATTER), ZONE_ID));
        task.setDuration(10);
        Task receivedTask = null;
        try {
            TaskManager fileBackedTasksManager =
                    Managers.getDefaultTaskManager(new File("Test_backup_file.csv"));
            fileBackedTasksManager.deleteAllItems();
            fileBackedTasksManager.addNewTask(task);
            TaskManager anotherFileBackedTaskManager =
                    FileBackedTasksManager.loadFromFile(new File("Test_backup_file.csv"));
            receivedTask = anotherFileBackedTaskManager.getTaskById(1);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        assertEquals(task, receivedTask, "Loaded task is not equal to added task!");
    }

    /**
     * Получение задачи со стандартным поведением.
     */
    @Test
    void getTask() {
        Task task = new Task();
        task.setTitle("Test_Title");
        task.setDescription("Test_Description");
        task.setStatus(Status.IN_PROGRESS);
        task.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:00", DT_FORMATTER), ZONE_ID));
        task.setDuration(10);
        Task receivedTask = null;
        try {
            manager.addNewTask(task);
            receivedTask = manager.getTask(1);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        task.setId(1);
        assertEquals(task, receivedTask, "Added task is not equal to received task!");
    }

    /**
     * Получение задачи с пустым списком задач.
     */
    @Test
    void whenTryToGetTaskAndThereAreNoTasksShouldBeThrownAnException() {
        String exceptionMessage = null;
        try {
            manager.getTask(6);
        } catch (NoSuchTaskException e) {
            exceptionMessage = e.getMessage();
        }
        assertEquals("There's no task with such id!", exceptionMessage, "Exception wasn't thrown!");
    }

    /**
     * Получение задачи с без идентификатора.
     */
    @Test
    void whenTryToGetTaskWithNoIdShouldBeThrownAnException() {
        String exceptionMessage = null;
        try {
            manager.getTask(0);
        } catch (NoSuchTaskException e) {
            exceptionMessage = e.getMessage();
        }
        assertEquals("Task has no id!", exceptionMessage, "Exception wasn't thrown!");
    }

    /**
     * Получение эпика со стандартным поведением.
     */
    @Test
    void getEpic() {
        Epic epic = new Epic();
        epic.setTitle("Test_Title_1");
        epic.setDescription("Test_Description_1");
        Epic receivedEpic = null;
        try {
            manager.addNewEpic(epic);
            receivedEpic = manager.getEpic(1);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        epic.setId(1);
        epic.setStartTime(null);
        epic.setDuration(0);
        epic.setEndTime(null);
        assertEquals(epic, receivedEpic, "Added epic is not equal to received epic!");
    }

    /**
     * Получение эпика с пустым списком задач.
     */
    @Test
    void whenTryToGetEpicAndThereAreNoEpicsShouldBeThrownAnException() {
        String exceptionMessage = null;
        try {
            manager.getEpic(6);
        } catch (NoSuchTaskException e) {
            exceptionMessage = e.getMessage();
        }
        assertEquals("There's no task with such id!", exceptionMessage, "Exception wasn't thrown!");
    }

    /**
     * Получение эпика с без идентификатора.
     */
    @Test
    void whenTryToGetEpicWithNoIdShouldBeThrownAnException() {
        String exceptionMessage = null;
        try {
            manager.getEpic(0);
        } catch (NoSuchTaskException e) {
            exceptionMessage = e.getMessage();
        }
        assertEquals("Task has no id!", exceptionMessage, "Exception wasn't thrown!");
    }

    /**
     * Получение подзадачи со стандартным поведением.
     */
    @Test
    void getSubtask() {
        Epic epic = new Epic();
        Subtask subtask = new Subtask();
        subtask.setTitle("Test_Title_1");
        subtask.setDescription("Test_Description_1");
        subtask.setStatus(Status.IN_PROGRESS);
        subtask.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:00", DT_FORMATTER), ZONE_ID));
        subtask.setDuration(10);
        subtask.setEpicId(1);
        Subtask receivedSubtask = null;
        try {
            manager.addNewEpic(epic);
            manager.addNewSubtask(subtask);
            receivedSubtask = manager.getSubtask(2);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        subtask.setId(2);
        assertEquals(subtask, receivedSubtask, "Added subtask is not equal to received subtask!");
    }

    /**
     * Получение подзадачи с пустым списком задач.
     */
    @Test
    void whenTryToGetSubtaskAndThereAreNoSubtasksShouldBeThrownAnException() {
        String exceptionMessage = null;
        try {
            manager.getSubtask(666);
        } catch (NoSuchTaskException e) {
            exceptionMessage = e.getMessage();
        }
        assertEquals("There's no task with such id!", exceptionMessage, "Exception wasn't thrown!");
    }

    /**
     * Получение подзадачи без идентификатора.
     */
    @Test
    void whenTryToGetSubtaskWithNoIdShouldBeThrownAnException() {
        String exceptionMessage = null;
        try {
            manager.getSubtask(0);
        } catch (NoSuchTaskException e) {
            exceptionMessage = e.getMessage();
        }
        assertEquals("Task has no id!", exceptionMessage, "Exception wasn't thrown!");
    }

    /**
     * Обновление задачи со стандартным поведением.
     */
    @Test
    void updateTask() {
        Task task = new Task();
        task.setTitle("Test_Title_1");
        task.setDescription("Test_Description_1");
        task.setStatus(Status.IN_PROGRESS);
        task.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:00", DT_FORMATTER), ZONE_ID));
        task.setDuration(10);
        try {
            manager.addNewTask(task);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        Task newTask = new Task();
        newTask.setId(1);
        newTask.setTitle("Test_Updated_Title");
        newTask.setDescription("Test_Updated_Description");
        newTask.setStatus(Status.DONE);
        newTask.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:05", DT_FORMATTER), ZONE_ID));
        newTask.setDuration(20);
        Task updatedTask = null;
        try {
            manager.updateTask(newTask);
            updatedTask = manager.getTask(1);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        assertEquals(newTask, updatedTask, "Updated task is not equal to received task!");
    }

    /**
     * Обновление задачи с пустым списком задач.
     */
    @Test
    void whenTryToUpdateTaskAndThereAreNoTasksShouldBeThrownAnException() {
        String exceptionMessage = null;
        try {
            manager.getTask(6);
        } catch (NoSuchTaskException e) {
            exceptionMessage = e.getMessage();
        }
        assertEquals("There's no task with such id!", exceptionMessage, "Exception wasn't thrown!");
    }

    /**
     * Обновление задачи без идентификатора.
     */
    @Test
    void whenTryToUpdateTaskWithNoIdShouldBeThrownAnException() {
        String exceptionMessage = null;
        try {
            manager.getTask(0);
        } catch (NoSuchTaskException e) {
            exceptionMessage = e.getMessage();
        }
        assertEquals("Task has no id!", exceptionMessage, "Exception wasn't thrown!");
    }

    /**
     * Добавление новой задачи со стандартным поведением.
     */
    @Test
    void addNewTask() {
        Task task = new Task();
        task.setTitle("Test_Title");
        task.setDescription("Test_Description");
        task.setStatus(Status.IN_PROGRESS);
        task.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:00", DT_FORMATTER), ZONE_ID));
        task.setDuration(10);
        Task receivedTask = null;
        try {
            manager.addNewTask(task);
            receivedTask = manager.getTask(1);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        assertEquals(task, receivedTask, "Added task is not equal to received task!");
    }

    /**
     * Добавление новой задачи с неверным идентификатором задачи.
     */
    @Test
    void WhenAddingNewTaskWithSpecifiedIdTheIdShouldBeOverwrote() {
        Task task = new Task();
        task.setId(666);
        List<Task> receivedTaskList = null;
        try {
            manager.addNewTask(task);
            receivedTaskList = manager.getAllItems();
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        assert receivedTaskList != null;
        assertEquals(1, receivedTaskList.get(0).getId(), "Id was not overwrote!");
    }

    /**
     * Добавление новой задачи с пересекающимся временем исполнения существующей задачи.
     */
    @Test
    void whenTryToAddTaskWithIntersectedRuntimeOfAnExistingTaskShouldBeThrownAnException() {
        Task task1 = new Task();
        task1.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:00", DT_FORMATTER), ZONE_ID));
        task1.setDuration(10);
        Task task2 = new Task();
        task2.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:05", DT_FORMATTER), ZONE_ID));
        task2.setDuration(10);
        String exceptionMessage = null;
        try {
            manager.addNewTask(task1);
            manager.addNewTask(task2);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            exceptionMessage = e.getMessage();
        }
        assertEquals("The adding task intersects in execution time with an existing task!", exceptionMessage,
                "Adding task was added despite intersecting the existing task!");
    }

    /**
     * Обновление эпика со стандартным поведением.
     */
    @Test
    void updateEpic() {
        Epic epic = new Epic();
        epic.setTitle("Test_Title");
        epic.setDescription("Test_Description");
        try {
            manager.addNewEpic(epic);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        Epic newEpic = new Epic();
        newEpic.setId(1);
        newEpic.setTitle("Test_Updated_Title");
        newEpic.setDescription("Test_Updated_Description");
        Epic updatedEpic = null;
        try {
            manager.updateEpic(newEpic);
            updatedEpic = manager.getEpic(1);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        assertEquals(epic, updatedEpic, "Updated epic is not equal to received epic!");
    }

    /**
     * Обновление эпика со списком подзадач.
     */
    @Test
    void whenTryToUpdateEpicWithSubtaskListThisListShouldBeCleared() {
        Epic epic1 = new Epic();
        try {
            manager.addNewEpic(epic1);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        Epic epic2 = new Epic();
        epic2.setId(1);
        epic2.setSubTasksIdList(List.of(1, 2, 3, 4, 5));
        try {
            manager.updateEpic(epic2);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        Epic recivedEpic = null;
        try {
            recivedEpic = manager.getEpic(1);
        } catch (NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        assert recivedEpic != null;
        assertTrue(recivedEpic.getSubTasksIdList().isEmpty(), "Subtask list of the updating epic wasn't cleared!");
    }

    /**
     * Обновление эпика с указанием startTime и duration.
     */
    @Test
    void whenTryToUpdateEpicWithStartTimeAndDurationTheyShouldBeCleared() {
        Epic epic1 = new Epic();
        try {
            manager.addNewEpic(epic1);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        Epic newEpic = new Epic();
        newEpic.setId(1);
        newEpic.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:00", DT_FORMATTER), ZONE_ID));
        newEpic.setDuration(10);
        Epic recivedEpic = null;
        try {
            manager.updateEpic(newEpic);
            recivedEpic = manager.getEpic(1);
        } catch (NoSuchTaskException | AddingAndUpdatingException e) {
            System.out.println(e.getMessage());
        }
        assert recivedEpic != null;
        assertNull(recivedEpic.getStartTime(), "Start time of the updated epic wasn't cleared!");
        assertEquals(0, recivedEpic.getDuration(), "Duration of the updated epic wasn't cleared!");
    }

    /**
     * Добавление нового эпика со стандартным поведением.
     */
    @Test
    void addNewEpic() {
        Epic epic = new Epic();
        epic.setTitle("Test_Title");
        epic.setDescription("Test_Description");
        Epic receivedEpic = null;
        try {
            manager.addNewEpic(epic);
            receivedEpic = manager.getEpic(1);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        assertEquals(epic, receivedEpic, "Added epic is not equal to received epic!");
    }

    /**
     * Добавление нового эпика со списком подзадач.
     */
    @Test
    void whenTryToAddNewEpicWithSubtaskListThisListShouldBeCleared() {
        Epic epic = new Epic();
        epic.setSubTasksIdList(List.of(1, 2, 3, 4, 5));
        Epic recivedEpic = null;
        try {
            manager.addNewEpic(epic);
            recivedEpic = manager.getEpic(1);
        } catch (NoSuchTaskException | AddingAndUpdatingException e) {
            System.out.println(e.getMessage());
        }
        assert recivedEpic != null;
        assertTrue(recivedEpic.getSubTasksIdList().isEmpty(), "Subtask list of the adding epic wasn't cleared!");
    }

    /**
     * Добавление нового эпика с указанием startTime и duration.
     */
    @Test
    void whenTryToAddNewEpicWithStartTimeAndDurationTheyShouldBeCleared() {
        Epic epic = new Epic();
        epic.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:00", DT_FORMATTER), ZONE_ID));
        epic.setDuration(10);
        Epic receivedEpic = null;
        try {
            manager.addNewEpic(epic);
            receivedEpic = manager.getEpic(1);
        } catch (NoSuchTaskException | AddingAndUpdatingException e) {
            System.out.println(e.getMessage());
        }
        assert receivedEpic != null;
        assertNull(receivedEpic.getStartTime(), "Start time of the added epic wasn't cleared!");
        assertEquals(0, receivedEpic.getDuration(), "Duration of the added epic wasn't cleared!");
    }

    /**
     * Добавление нового эпика с указанием статуса в отсутсвие подзадач.
     */
    @Test
    void whenTryToAddNewEpicWithSomeStatusItShouldBeNewWhenThereAreNoSubtasks() {
        Epic epic = new Epic();
        epic.setStatus(Status.IN_PROGRESS);
        Epic recivedEpic = null;
        try {
            manager.addNewEpic(epic);
            recivedEpic = manager.getEpic(1);
        } catch (NoSuchTaskException | AddingAndUpdatingException e) {
            System.out.println(e.getMessage());
        }
        assert recivedEpic != null;
        assertEquals(Status.NEW, recivedEpic.getStatus(), "Status wasn't updated!");
    }

    /**
     * Рассчет статуса эпика (все подзадачи со статусом NEW).
     */
    @Test
    void whenAllSubtasksOfAnEpicAreNewEpicStatusShouldBeNew() {
        Epic epic = new Epic();
        Subtask subtask1 = new Subtask();
        Subtask subtask2 = new Subtask();
        Subtask subtask3 = new Subtask();
        subtask1.setEpicId(1);
        subtask2.setEpicId(1);
        subtask3.setEpicId(1);
        subtask1.setStatus(Status.NEW);
        subtask2.setStatus(Status.NEW);
        subtask3.setStatus(Status.NEW);
        try {
            manager.addNewEpic(epic);
            manager.addNewSubtask(subtask1);
            manager.addNewSubtask(subtask2);
            manager.addNewSubtask(subtask3);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        Epic receivedEpic = null;
        try {
            receivedEpic = manager.getEpic(1);
        } catch (NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        assert receivedEpic != null;
        assertEquals(Status.NEW, receivedEpic.getStatus(), "Epic status is not NEW!");
    }

    /**
     * Рассчет статуса эпика (все подзадачи со статусом DONE).
     */
    @Test
    void whenAllSubtasksOfAnEpicAreDoneEpicStatusShouldBeDone() {
        Epic epic = new Epic();
        Subtask subtask1 = new Subtask();
        Subtask subtask2 = new Subtask();
        Subtask subtask3 = new Subtask();
        subtask1.setEpicId(1);
        subtask2.setEpicId(1);
        subtask3.setEpicId(1);
        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        subtask3.setStatus(Status.DONE);
        try {
            manager.addNewEpic(epic);
            manager.addNewSubtask(subtask1);
            manager.addNewSubtask(subtask2);
            manager.addNewSubtask(subtask3);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        Epic receivedEpic = null;
        try {
            receivedEpic = manager.getEpic(1);
        } catch (NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        assert receivedEpic != null;
        assertEquals(Status.DONE, receivedEpic.getStatus(), "Epic status is not DONE!");
    }

    /**
     * Рассчет статуса эпика (подзадачи со статусами NEW, IN_PROGRESS и DONE).
     */
    @Test
    void whenSubtasksOfAnEpicAreNewInProgressDoneEpicStatusShouldBeInProgress() {
        Epic epic = new Epic();
        Subtask subtask1 = new Subtask();
        Subtask subtask2 = new Subtask();
        Subtask subtask3 = new Subtask();
        subtask1.setEpicId(1);
        subtask2.setEpicId(1);
        subtask3.setEpicId(1);
        subtask1.setStatus(Status.NEW);
        subtask2.setStatus(Status.IN_PROGRESS);
        subtask3.setStatus(Status.DONE);
        try {
            manager.addNewEpic(epic);
            manager.addNewSubtask(subtask1);
            manager.addNewSubtask(subtask2);
            manager.addNewSubtask(subtask3);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        Epic receivedEpic = null;
        try {
            receivedEpic = manager.getEpic(1);
        } catch (NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        assert receivedEpic != null;
        assertEquals(Status.IN_PROGRESS, receivedEpic.getStatus(), "Epic status is not IN_PROGRESS!");
    }

    /**
     * Расчет длительности, времени старта и окончания эпика по подзадачам
     */
    @Test
    void whenAddingSubtasksEpicTemporalFieldsAndDurationShouldChange() {
        Epic epic = new Epic();
        Subtask subtask1 = new Subtask();
        Subtask subtask2 = new Subtask();
        Subtask subtask3 = new Subtask();
        subtask1.setEpicId(1);
        subtask2.setEpicId(1);
        subtask3.setEpicId(1);
        subtask1.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:00", DT_FORMATTER), ZONE_ID));
        subtask1.setDuration(5);
        subtask2.setStartTime(ZonedDateTime.of(LocalDateTime.parse("02.01.2023 12:00", DT_FORMATTER), ZONE_ID));
        subtask2.setDuration(25);
        subtask3.setStartTime(ZonedDateTime.of(LocalDateTime.parse("03.01.2023 01:30", DT_FORMATTER), ZONE_ID));
        subtask3.setDuration(30);
        Epic receivedEpic = null;
        try {
            manager.addNewEpic(epic);
            manager.addNewSubtask(subtask1);
            manager.addNewSubtask(subtask2);
            manager.addNewSubtask(subtask3);
            receivedEpic = manager.getEpic(1);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        assert receivedEpic != null;
        assertEquals(subtask1.getStartTime(), receivedEpic.getStartTime(), "Received epic has wrong start time!");
        assertEquals(60, receivedEpic.getDuration(), "Received epic has wrong duration!");
        assertEquals(subtask3.getEndTime(), receivedEpic.getEndTime(), "Received epic has wrong end time!");
    }

    /**
     * Перерасчет длительности, времени старта и окончания эпика после удаления подзадачам
     */
    @Test
    void whenDeleteASubtaskOfAnEpicTemporalFieldsAndDurationShouldChange() {
        Epic epic = new Epic();
        Subtask subtask1 = new Subtask();
        Subtask subtask2 = new Subtask();
        Subtask subtask3 = new Subtask();
        subtask1.setEpicId(1);
        subtask2.setEpicId(1);
        subtask3.setEpicId(1);
        subtask1.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:00", DT_FORMATTER), ZONE_ID));
        subtask1.setDuration(5);
        subtask2.setStartTime(ZonedDateTime.of(LocalDateTime.parse("02.01.2023 12:00", DT_FORMATTER), ZONE_ID));
        subtask2.setDuration(25);
        subtask3.setStartTime(ZonedDateTime.of(LocalDateTime.parse("03.01.2023 01:30", DT_FORMATTER), ZONE_ID));
        subtask3.setDuration(30);
        Epic receivedEpic = null;
        try {
            manager.addNewEpic(epic);
            manager.addNewSubtask(subtask1);
            manager.addNewSubtask(subtask2);
            manager.addNewSubtask(subtask3);
            manager.deleteTaskById(2);
            receivedEpic = manager.getEpic(1);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        assert receivedEpic != null;
        assertEquals(subtask2.getStartTime(), receivedEpic.getStartTime(), "Received epic has wrong start time!");
        assertEquals(55, receivedEpic.getDuration(), "Received epic has wrong duration!");
        assertEquals(subtask3.getEndTime(), receivedEpic.getEndTime(), "Received epic has wrong end time!");
    }

    /**
     * Перерасчет статуса эпика после удаления подзадачи
     */
    @Test
    void whenDeleteASubtaskOfAnEpicItShouldChangeStatus() {
        Epic epic = new Epic();
        Subtask subtask = new Subtask();
        subtask.setEpicId(1);
        subtask.setStatus(Status.IN_PROGRESS);
        Task receivedEpic = null;
        try {
            manager.addNewEpic(epic);
            manager.addNewSubtask(subtask);
            manager.deleteTaskById(2);
            receivedEpic = manager.getTaskById(1);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        assert receivedEpic != null;
        assertEquals(Status.NEW, receivedEpic.getStatus(), "Epic status wasn't changed!");
    }

    /**
     * Обновление подзадачи со стандартным поведением.
     */
    @Test
    void updateSubtask() {
        Epic epic = new Epic();
        Subtask subtask = new Subtask();
        subtask.setTitle("Test_Title");
        subtask.setDescription("Test_Description");
        subtask.setStatus(Status.IN_PROGRESS);
        subtask.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:00", DT_FORMATTER), ZONE_ID));
        subtask.setDuration(10);
        subtask.setEpicId(1);
        try {
            manager.addNewEpic(epic);
            manager.addNewSubtask(subtask);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        Subtask newSubtask = new Subtask();
        newSubtask.setTitle("Test_Updated_Title");
        newSubtask.setDescription("Test_Updated_Description");
        newSubtask.setStatus(Status.DONE);
        newSubtask.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:05", DT_FORMATTER), ZONE_ID));
        newSubtask.setDuration(20);
        newSubtask.setId(2);
        newSubtask.setEpicId(1);
        Subtask updatedSubtask = null;
        try {
            manager.updateSubtask(newSubtask);
            updatedSubtask = manager.getSubtask(2);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        assertEquals(newSubtask, updatedSubtask, "Updated subtask is not equal to received subtask!");
    }

    /**
     * Обновление подзадачи с пересекающимся временем исполнения существующей задачи.
     */
    @Test
    void whenTryToUpdateSubtaskWithIntersectedRuntimeOfAnExistingTaskShouldBeThrownAnException() {
        Task task = new Task();
        task.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:00", DT_FORMATTER), ZONE_ID));
        task.setDuration(10);
        Subtask subtask = new Subtask();
        subtask.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:10", DT_FORMATTER), ZONE_ID));
        subtask.setDuration(10);
        Subtask newSubtask = new Subtask();
        subtask.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:09", DT_FORMATTER), ZONE_ID));
        subtask.setDuration(10);
        String exceptionMessage = null;
        try {
            manager.addNewTask(task);
            manager.addNewSubtask(subtask);
            manager.updateSubtask(newSubtask);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            exceptionMessage = e.getMessage();
        }
        assertEquals("The adding task intersects in execution time with an existing task!", exceptionMessage,
                "Updating subtask was updated despite intersecting the existing task!");
    }

    /**
     * Обновление подзадачи без указания id.
     */
    @Test
    void whenTryToUpdateSubtaskWithNoIdShouldBeThrownAnException() {
        Epic epic = new Epic();
        Subtask subtask = new Subtask();
        Subtask newSubtask = new Subtask();
        newSubtask.setId(2);
        String exceptionMessage = null;
        try {
            manager.addNewEpic(epic);
            manager.addNewSubtask(subtask);
            manager.updateSubtask(newSubtask);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            exceptionMessage = e.getMessage();
        }
        assertEquals("Unable to add subtask! There's no epics with such epicId", exceptionMessage,
                "Exception wasn't thrown!");

    }

    /**
     * Добавление новой подзадачи со стандартным поведением.
     */
    @Test
    void addNewSubtask() {
        Epic epic = new Epic();
        Subtask subtask = new Subtask();
        subtask.setTitle("Test_Title");
        subtask.setDescription("Test_Description");
        subtask.setStatus(Status.IN_PROGRESS);
        subtask.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:00", DT_FORMATTER), ZONE_ID));
        subtask.setDuration(10);
        subtask.setEpicId(1);
        Subtask receivedSubtask = null;
        try {
            manager.addNewEpic(epic);
            manager.addNewSubtask(subtask);
            receivedSubtask = manager.getSubtask(2);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        assertEquals(subtask, receivedSubtask, "Added subtask is not equal to received subtask!");
    }

    /**
     * Добавление новой подзадачи в отсуствие существующих эпиков.
     */
    @Test
    void whenTryToAddSubtaskWithoutExistingEpicsShouldBeThrownAnException() {
        Subtask subtask = new Subtask();
        String exceptionMessage = null;
        try {
            manager.addNewSubtask(subtask);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            exceptionMessage = e.getMessage();
        }
        assertEquals("Unable to add subtask! There's no epics!", exceptionMessage,
                "Exception wasn't thrown!");
    }

    /**
     * Добавление новой подзадачи с пересекающимся временем исполнения существующей задачи.
     */
    @Test
    void whenTryToAddSubtaskWithIntersectedRuntimeOfAnExistingTaskShouldBeThrownAnException() {
        Task task = new Task();
        task.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:00", DT_FORMATTER), ZONE_ID));
        task.setDuration(10);
        Subtask subtask = new Subtask();
        subtask.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:09", DT_FORMATTER), ZONE_ID));
        subtask.setDuration(10);
        String exceptionMessage = null;
        try {
            manager.addNewTask(task);
            manager.addNewSubtask(subtask);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            exceptionMessage = e.getMessage();
        }
        assertEquals("The adding task intersects in execution time with an existing task!", exceptionMessage,
                "Adding subtask was added despite intersecting the existing task!");
    }

    /**
     * Получение списка всех задач о стандартным поведением.
     */
    @Test
    void getAllItems() {
        Task task = new Task();
        Epic epic = new Epic();
        Subtask subtask = new Subtask();
        subtask.setEpicId(2);
        List<Task> taskList = List.of(task, epic, subtask);
        List<Task> receivedTaskList = null;
        try {
            manager.addNewTask(task);
            manager.addNewEpic(epic);
            manager.addNewSubtask(subtask);
            receivedTaskList = manager.getAllItems();
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        assertEquals(taskList, receivedTaskList, "Added tasks are not equal to received tasks!");
    }

    /**
     * Получение списка всех задач в отсутствие задач.
     */
    @Test
    void whenTryToGetAllItemsInTheirAbsenceShouldReceiveAnEmptyList() {
        List<Task> taskList = manager.getAllItems();
        assertTrue(taskList.isEmpty(), "Received no existing tasks!");
    }

    /**
     * Получение списка всех задач о стандартным поведением.
     */
    @Test
    void getAllTasks() {
        Task task1 = new Task();
        Task task2 = new Task();
        Task task3 = new Task();
        Epic epic = new Epic();
        Subtask subtask = new Subtask();
        subtask.setEpicId(4);
        List<Task> taskList = List.of(task1, task2, task3);
        List<Task> receivedTaskList = null;
        try {
            manager.addNewTask(task1);
            manager.addNewTask(task2);
            manager.addNewTask(task3);
            manager.addNewEpic(epic);
            manager.addNewSubtask(subtask);
            receivedTaskList = manager.getAllTasks();
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        assertEquals(taskList, receivedTaskList, "Received tasks are not equal to added tasks!");
    }

    /**
     * Получение списка всех задач в отсутствие задач.
     */
    @Test
    void whenTryToGetAllTasksInTheirAbsenceShouldReceiveAnEmptyList() {
        List<Task> taskList = manager.getAllTasks();
        assertTrue(taskList.isEmpty(), "Received no existing tasks!");
    }

    /**
     * Получение списка эпиков о стандартным поведением.
     */
    @Test
    void getAllEpics() {
        Epic epic1 = new Epic();
        Epic epic2 = new Epic();
        Epic epic3 = new Epic();
        Task task = new Task();
        Subtask subtask = new Subtask();
        subtask.setEpicId(1);
        List<Epic> taskList = List.of(epic1, epic2, epic3);
        List<Epic> receivedTaskList = null;
        try {
            manager.addNewEpic(epic1);
            manager.addNewEpic(epic2);
            manager.addNewEpic(epic3);
            manager.addNewTask(task);
            manager.addNewSubtask(subtask);
            receivedTaskList = manager.getAllEpics();
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        assertEquals(taskList, receivedTaskList, "Added tasks are not equal to received tasks!");
    }

    /**
     * Получение списка эпиков в отсутствие эпиков.
     */
    @Test
    void whenTryToGetAllEpicsInTheirAbsenceShouldReceiveAnEmptyList() {
        List<Epic> epicList = manager.getAllEpics();
        assertTrue(epicList.isEmpty(), "Received no existing epics!");
    }

    /**
     * Получение списка подзадач о стандартным поведением.
     */
    @Test
    void getAllSubtasks() {
        Task task = new Task();
        Epic epic = new Epic();
        Subtask subtask1 = new Subtask();
        Subtask subtask2 = new Subtask();
        Subtask subtask3 = new Subtask();
        subtask1.setEpicId(2);
        subtask2.setEpicId(2);
        subtask3.setEpicId(2);
        List<Subtask> subtaskList = List.of(subtask1, subtask2, subtask3);
        List<Subtask> receivedList = null;
        try {
            manager.addNewTask(task);
            manager.addNewEpic(epic);
            manager.addNewSubtask(subtask1);
            manager.addNewSubtask(subtask2);
            manager.addNewSubtask(subtask3);
            receivedList = manager.getAllSubtasks();
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        assertEquals(subtaskList, receivedList, "Added subtasks are not equal to received subtasks!");
    }

    /**
     * Получение списка задач по типу со стандартным поведением.
     */
    @Test
    void getTasksByType() {
        Task task1 = new Task();
        Task task2 = new Task();
        Task task3 = new Task();
        Epic epic1 = new Epic();
        Epic epic2 = new Epic();
        Epic epic3 = new Epic();
        Subtask subtask1 = new Subtask();
        Subtask subtask2 = new Subtask();
        Subtask subtask3 = new Subtask();
        subtask1.setEpicId(4);
        subtask2.setEpicId(4);
        subtask3.setEpicId(4);
        List<Task> taskList = List.of(task1, task2, task3);
        List<Epic> epicList = List.of(epic1, epic2, epic3);
        List<Subtask> subtaskList = List.of(subtask1, subtask2, subtask3);
        List<Task> receivedTaskList = null;
        List<Task> receivedEpicList = null;
        List<Task> receivedSubtaskList = null;
        try {
            manager.addNewTask(task1);
            manager.addNewTask(task2);
            manager.addNewTask(task3);
            manager.addNewEpic(epic1);
            manager.addNewEpic(epic2);
            manager.addNewEpic(epic3);
            manager.addNewSubtask(subtask1);
            manager.addNewSubtask(subtask2);
            manager.addNewSubtask(subtask3);
            receivedTaskList = manager.getTasksByType(Type.TASK);
            receivedEpicList = manager.getTasksByType(Type.EPIC);
            receivedSubtaskList = manager.getTasksByType(Type.SUBTASK);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        assertEquals(taskList, receivedTaskList, "Received task list is not equal to added task list!");
        assertEquals(epicList, receivedEpicList, "Received epic list is not equal to added epic list!");
        assertEquals(subtaskList, receivedSubtaskList, "Received subtask list is not equal to added subtask list!");
    }

    /**
     * Получение списка задач по типу в отсутсвие задач
     */
    @Test
    void whenTryToGetTasksByTypeInTheirAbsenceShouldReceiveAnEmptyList() {
        List<Task> receivedTaskList = manager.getTasksByType(Type.TASK);
        List<Task> receivedEpicList = manager.getTasksByType(Type.EPIC);
        List<Task> receivedSubtaskList = manager.getTasksByType(Type.SUBTASK);
        assertTrue(receivedTaskList.isEmpty() && receivedEpicList.isEmpty() && receivedSubtaskList.isEmpty(),
                "Received no existing tasks!");
    }

    /**
     * Удаление списка всех задач со стандартным поведением.
     */
    @Test
    void deleteAllItems() {
        Task task = new Task();
        Epic epic = new Epic();
        Subtask subtask = new Subtask();
        subtask.setEpicId(2);
        try {
            manager.addNewTask(task);
            manager.addNewEpic(epic);
            manager.addNewSubtask(subtask);
            manager.deleteAllItems();
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        List<Task> receivedTaskList = manager.getAllItems();
        assertTrue(receivedTaskList.isEmpty(), "Deleting was not successful!");
    }

    /**
     * Удаление задачи по id со стандартным поведением
     */
    @Test
    void deleteTaskById() {
        Task task = new Task();
        Epic epic = new Epic();
        Subtask subtask = new Subtask();
        subtask.setEpicId(2);
        String exceptionMessage = null;
        try {
            manager.addNewTask(task);
            manager.addNewEpic(epic);
            manager.addNewSubtask(subtask);
            manager.deleteTaskById(1);
            manager.getTaskById(1);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            exceptionMessage = e.getMessage();
        }
        assertEquals("There's no task with such id!", exceptionMessage, "Task wasn't deleted!");
    }

    /**
     * Удаление задачи по id в отсутствие задачи
     */
    @Test
    void whenTryToDeleteNoExistingTaskByIdShouldBeThrownAnException() {
        String exceptionMessage = null;
        try {
            manager.getTaskById(1);
        } catch (NoSuchTaskException e) {
            exceptionMessage = e.getMessage();
        }
        assertEquals("There's no task with such id!", exceptionMessage, "Exception wasn't thrown!");
    }

    /**
     * Удаление списка задач по типу со стандартным поведением.
     */
    @Test
    void deleteTasksByType() {
        Task task1 = new Task();
        Task task2 = new Task();
        Task task3 = new Task();
        Epic epic1 = new Epic();
        Epic epic2 = new Epic();
        Epic epic3 = new Epic();
        Subtask subtask1 = new Subtask();
        Subtask subtask2 = new Subtask();
        Subtask subtask3 = new Subtask();
        subtask1.setEpicId(4);
        subtask2.setEpicId(4);
        subtask3.setEpicId(4);
        List<Task> receivedTaskList = null;
        List<Task> receivedEpicList = null;
        List<Task> receivedSubtaskList = null;
        try {
            manager.addNewTask(task1);
            manager.addNewTask(task2);
            manager.addNewTask(task3);
            manager.addNewEpic(epic1);
            manager.addNewEpic(epic2);
            manager.addNewEpic(epic3);
            manager.addNewSubtask(subtask1);
            manager.addNewSubtask(subtask2);
            manager.addNewSubtask(subtask3);
            manager.deleteTasksByType(Type.TASK);
            manager.deleteTasksByType(Type.EPIC);
            manager.deleteTasksByType(Type.SUBTASK);
            receivedTaskList = manager.getTasksByType(Type.TASK);
            receivedEpicList = manager.getTasksByType(Type.EPIC);
            receivedSubtaskList = manager.getTasksByType(Type.SUBTASK);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        assert receivedTaskList != null;
        assertTrue(receivedTaskList.isEmpty(), "Received task list is not empty!");
        assert receivedEpicList != null;
        assertTrue(receivedEpicList.isEmpty(), "Received epic list is not empty!");
        assert receivedSubtaskList != null;
        assertTrue(receivedSubtaskList.isEmpty(), "Received subtask list is not empty!");
    }

    /**
     * Получение задачи по id со стандартным поведением.
     */
    @Test
    void getTaskById() {
        Task task1 = new Task();
        Task task2 = new Task();
        Task task3 = new Task();
        Task receivedTask = null;
        try {
            manager.addNewTask(task1);
            manager.addNewTask(task2);
            manager.addNewTask(task3);
            receivedTask = manager.getTaskById(2);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        assertEquals(task2, receivedTask, "Received task is not equal to added task");
    }

    /**
     * Получение задачи по id в отсутствие задачи.
     */
    @Test
    void whenTryToGetNoExistingTaskByIdShouldBeThrownAnException() {
        String exceptionMessage = null;
        try {
            manager.getTaskById(666);
        } catch (NoSuchTaskException e) {
            exceptionMessage = e.getMessage();
        }
        assertEquals("There's no task with such id!", exceptionMessage, "Exception wasn't thrown!");
    }

    /**
     * Получение спика подзадач эпика со стандартным поведением.
     */
    @Test
    void getEpicsSubtasksById() {
        Epic epic = new Epic();
        Subtask subtask1 = new Subtask();
        Subtask subtask2 = new Subtask();
        Subtask subtask3 = new Subtask();
        subtask1.setEpicId(1);
        subtask2.setEpicId(1);
        subtask3.setEpicId(1);
        List<Subtask> subtaskList = List.of(subtask1, subtask2, subtask3);
        List<Subtask> receivedList = null;
        try {
            manager.addNewEpic(epic);
            manager.addNewSubtask(subtask1);
            manager.addNewSubtask(subtask2);
            manager.addNewSubtask(subtask3);
            receivedList = manager.getEpicsSubtasksById(1);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        assertEquals(subtaskList, receivedList, "Received list is not equal to added list");
    }

    /**
     * Получение списка подзадач эпика в отсустсвие подзадач.
     */
    @Test
    void whenTryToGetSubtaskListOfAnEpicWithoutSubtasksShouldBeThrownAnException() {
        Epic epic = new Epic();
        String exceptionMessage = null;
        try {
            manager.addNewEpic(epic);
            manager.getEpicsSubtasksById(1);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            exceptionMessage = e.getMessage();
        }
        assertEquals("Epic has no subtasks!", exceptionMessage, "Exception wasn't thrown!");
    }

    /**
     * Получение списка приоритетных задач со стандартным поведением.
     */
    @Test
    void getPrioritizedTasks() {
        Task task1 = new Task();
        task1.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:00", DT_FORMATTER), ZONE_ID));
        task1.setDuration(10);
        Task task2 = new Task();
        task2.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:10", DT_FORMATTER), ZONE_ID));
        task2.setDuration(10);
        Epic epic1 = new Epic();
        Epic epic2 = new Epic();
        Epic epic3 = new Epic();
        Subtask subtask1 = new Subtask();
        subtask1.setEpicId(3);
        subtask1.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:20", DT_FORMATTER), ZONE_ID));
        subtask1.setDuration(10);
        Subtask subtask2 = new Subtask();
        subtask2.setEpicId(3);
        subtask2.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:30", DT_FORMATTER), ZONE_ID));
        subtask2.setDuration(10);
        List<Task> taskList = List.of(task1, task2, subtask1, epic1, subtask2, epic3, epic2);
        List<Task> allTasksList = null;
        List<Task> receivedTaskList = null;
        try {
            manager.addNewTask(task1);
            manager.addNewTask(task2);
            manager.addNewEpic(epic1);
            manager.addNewEpic(epic2);
            manager.addNewEpic(epic3);
            manager.addNewSubtask(subtask1);
            manager.addNewSubtask(subtask2);
            allTasksList = manager.getAllItems();
            receivedTaskList = manager.getPrioritizedTasks();
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
        assert allTasksList != null;
        assertEquals(allTasksList.size(), receivedTaskList.size(), "Received tasks quantity is not equal to all tasks");
        assertEquals(taskList, receivedTaskList, "Received tasks are in wrong order");
    }

    /**
     * Получение списка приоритетных задач в отсутствие задач.
     */
    @Test
    void whenTryToGetPrioritizedTasksInTheirAbsenceShouldReceiveAnEmptyList() {
        List<Task> receivedTaskList = manager.getPrioritizedTasks();
        assertTrue(receivedTaskList.isEmpty(), "Received no existing tasks");
    }

}