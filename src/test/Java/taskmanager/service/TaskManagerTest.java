package taskmanager.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanager.model.*;
import taskmanager.exceptions.NoSuchTaskException;
import taskmanager.exceptions.AddingAndUpdatingException;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    public static DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    public static ZoneId ZONE_ID = ZoneId.of("Europe/Moscow");

    private T manager;

    abstract T getManager();

    @BeforeEach
    public void setUp() {
        manager = getManager();
        manager.deleteAllItems();
    }

    /**
     * Загрузка менеджера со стандартным поведением.
     */
    @Test
    public void whenTryToLoadFromFileAllLoadedShouldBeEqualToCurrentManager() {
        List<Task> addingTasks = createSomeTasks();
        manager.addNewTask(addingTasks.get(0));
        manager.addNewEpic((Epic) addingTasks.get(1));
        manager.addNewSubtask((Subtask) addingTasks.get(2));
        manager.getTaskById(1);
        manager.getTaskById(2);
        manager.getTaskById(3);
        TaskManager loadedManager;
        if (manager instanceof InMemoryTaskManagerTest) {
            loadedManager = FileBackedTasksManager.loadFromFile(new File("Test_backup_file.csv"));
        } else {
            loadedManager = manager;
            manager = (T) FileBackedTasksManager.loadFromFile(new File("Test_backup_file.csv"));
        }
        assertEquals(manager.getAllItems(), loadedManager.getAllItems(), "All items are not equal to loaded items!");
        assertEquals(manager.getHistory(), loadedManager.getHistory(), "History is not equal to loaded history!");
        assertEquals(manager.getPrioritizedTasks(), loadedManager.getPrioritizedTasks(),
                "Prioritized list is not equal to loaded prioritized list!");
        assertEquals(manager.getAllTasks(), loadedManager.getAllTasks(), "All tasks are not equal to loaded tasks!");
        assertEquals(manager.getAllEpics(), loadedManager.getAllEpics(), "All epics are not equal to loaded epics!");
        assertEquals(manager.getAllSubtasks(), loadedManager.getAllSubtasks(),
                "All subtasks are not equal to loaded subtasks!");
        assertEquals(manager.getIdField(), loadedManager.getIdField(), "Id field is not equal to loaded id field!");
    }

    /**
     * Получение истрии просмотров со стандартным поведением
     */
    @Test
    void getHistory() {
        List<Task> addingTasks = createSomeTasks();
        manager.addNewTask(addingTasks.get(0));
        manager.addNewEpic((Epic) addingTasks.get(1));
        manager.addNewSubtask((Subtask) addingTasks.get(2));
        manager.getTaskById(3);
        manager.getTaskById(2);
        manager.getTaskById(1);
        List<Task> controllList = manager.getAllItems();
        assertEquals(controllList, manager.getHistory(), "Received history is wrong!");
    }

    /**
     * Получение истории в отсутствие просмотров
     */
    @Test
    void whenTryToGetHistoryOfViewsInTheirAbsenceShouldReceiveAnEmptyList() {
        List<Task> addingTasks = createSomeTasks();
        manager.addNewTask(addingTasks.get(0));
        manager.addNewEpic((Epic) addingTasks.get(1));
        manager.addNewSubtask((Subtask) addingTasks.get(2));
        List<Task> receivedHistoryList = manager.getHistory();
        assertTrue(receivedHistoryList.isEmpty(), "Received not empty history list");
    }

    /**
     * Получение задачи из backup файла со стандарным поведением
     */
    @Test
    void whenUseLoadingFromFileAllTaskFieldsShouldBeLoadedCorrectly() {
        List<Task> addingTasks = createSomeTasks();
        manager.addNewTask(addingTasks.get(0));
        TaskManager loadedManager;
        if (manager instanceof InMemoryTaskManagerTest) {
            loadedManager = FileBackedTasksManager.loadFromFile(new File("Test_backup_file.csv"));
        } else {
            loadedManager = manager;
        }
        assertEquals(manager.getTaskById(1), loadedManager.getTaskById(1), "Loaded task is not equal to added task!");
    }

    /**
     * Получение задачи со стандартным поведением.
     */
    @Test
    void getTask() {
        List<Task> addingTasks = createSomeTasks();
        manager.addNewTask(addingTasks.get(0));
        TaskManager loadedManager;
        if (manager instanceof InMemoryTaskManagerTest) {
            loadedManager = FileBackedTasksManager.loadFromFile(new File("Test_backup_file.csv"));
        } else {
            loadedManager = manager;
        }
        assertEquals(manager.getTaskById(1), loadedManager.getTaskById(1), "Received task is not equal to added task!");
    }

    /**
     * Получение задачи с пустым списком задач.
     */
    @Test
    void whenTryToGetTaskAndThereAreNoTasksShouldBeThrownAnException() {
        String exceptionMessage = null;
        try {
            manager.getTask(666);
        } catch (NoSuchTaskException e) {
            exceptionMessage = e.getMessage();
        }
        assertEquals("There's no task with such id!", exceptionMessage, "Exception wasn't thrown!");
    }

    /**
     * Получение задачи без идентификатора.
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
        List<Task> addingTasks = createSomeTasks();
        manager.addNewEpic((Epic) addingTasks.get(1));
        assertEquals(addingTasks.get(1), manager.getEpic(1), "Epic wasn't received!");
    }

    /**
     * Получение эпика с пустым списком задач.
     */
    @Test
    void whenTryToGetEpicAndThereAreNoEpicsShouldBeThrownAnException() {
        String exceptionMessage = null;
        try {
            manager.getEpic(666);
        } catch (NoSuchTaskException e) {
            exceptionMessage = e.getMessage();
        }
        assertEquals("There's no task with such id!", exceptionMessage, "Exception wasn't thrown!");
    }

    /**
     * Получение эпика без идентификатора.
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
        List<Task> addingTasks = createSomeTasks();
        manager.addNewEpic((Epic) addingTasks.get(1));
        Subtask subtask = (Subtask) addingTasks.get(2);
        subtask.setEpicId(1);
        manager.addNewSubtask(subtask);
        assertEquals(addingTasks.get(2), manager.getSubtask(2), "Subtask wasn't received!");
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
        List<Task> addingTasks = createSomeTasks();
        manager.addNewTask(addingTasks.get(0));
        Task task = new Task();
        task.setId(1);
        task.setStatus(Status.NEW);
        task.setTitle("Updated_title");
        task.setDescription("Updated_description");
        task.setStartTime(ZonedDateTime.of(LocalDateTime.parse("02.02.2022 22:22", DT_FORMATTER), ZONE_ID));
        task.setDuration(20L);
        manager.updateTask(task);
        assertEquals(task, manager.getTaskById(1), "Task wasn't updated!");
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
        List<Task> addingTasks = createSomeTasks();
        manager.addNewTask(addingTasks.get(0));
        assertEquals(addingTasks.get(0), manager.getTaskById(1), "Received task is not equal to added task");
    }

    /**
     * Добавление новой задачи с неверным идентификатором задачи.
     */
    @Test
    void WhenAddingNewTaskWithSpecifiedIdTheIdShouldBeOverwrote() {
        List<Task> addingTasks = createSomeTasks();
        Task task = addingTasks.get(0);
        task.setId(666);
        manager.addNewTask(task);
        assertEquals(1, manager.getAllItems().get(0).getId(), "Id wasn't overwrote!");
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
        List<Task> addingTasks = createSomeTasks();
        manager.addNewEpic((Epic) addingTasks.get(1));
        Epic epic = new Epic();
        epic.setId(1);
        epic.setStatus(Status.NEW);
        epic.setTitle("Updated_title");
        epic.setDescription("Updated_description");
        manager.updateEpic(epic);
        assertEquals(epic, manager.getEpic(1), "Epic wasn't updated!");
    }

    /**
     * Обновление эпика со списком подзадач.
     */
    @Test
    void whenTryToUpdateEpicWithSubtaskListThisListShouldBeCleared() {
        List<Task> addingTasks = createSomeTasks();
        manager.addNewEpic((Epic) addingTasks.get(1));
        Epic epic = (Epic) addingTasks.get(1);
        epic.setId(1);
        epic.setSubTasksIdList(List.of(1, 2, 3, 4, 5));
        manager.updateEpic(epic);
        assertTrue(manager.getEpic(1).getSubTasksIdList().isEmpty(), "SubtaskList wasn't cleared!");
    }

    /**
     * Обновление эпика с указанием startTime и duration.
     */
    @Test
    void whenTryToUpdateEpicWithStartTimeAndDurationTheyShouldBeCleared() {
        List<Task> addingTasks = createSomeTasks();
        manager.addNewEpic((Epic) addingTasks.get(1));
        Epic epic = new Epic();
        epic.setId(1);
        epic.setStatus(Status.NEW);
        epic.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:00", DT_FORMATTER), ZONE_ID));
        epic.setDuration(10);
        manager.updateEpic(epic);
        assertNull(manager.getEpic(1).getStartTime(), "Start time of the updated epic wasn't cleared!");
        assertEquals(0, manager.getEpic(1).getDuration(), "Duration of the updated epic wasn't cleared!");
    }

    /**
     * Добавление нового эпика со стандартным поведением.
     */
    @Test
    void addNewEpic() {
        List<Task> addingTasks = createSomeTasks();
        manager.addNewEpic((Epic) addingTasks.get(1));
        assertEquals(addingTasks.get(1), manager.getEpic(1), "Epic wasn't added!");
    }

    /**
     * Добавление нового эпика со списком подзадач.
     */
    @Test
    void whenTryToAddNewEpicWithSubtaskListThisListShouldBeCleared() {
        List<Task> addingTasks = createSomeTasks();
        Epic epic = (Epic) addingTasks.get(1);
        epic.setSubTasksIdList(List.of(1, 2, 3, 4, 5));
        manager.addNewEpic(epic);
        assertTrue(manager.getEpic(1).getSubTasksIdList().isEmpty(),
                "Subtask list of the adding epic wasn't cleared!");
    }

    /**
     * Добавление нового эпика с указанием startTime и duration.
     */
    @Test
    void whenTryToAddNewEpicWithStartTimeAndDurationTheyShouldBeCleared() {
        List<Task> addingTasks = createSomeTasks();
        Epic epic = (Epic) addingTasks.get(1);
        epic.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:00", DT_FORMATTER), ZONE_ID));
        epic.setDuration(10);
        manager.addNewEpic(epic);
        assertNull(manager.getEpic(1).getStartTime(), "Start time of the added epic wasn't cleared!");
        assertEquals(0, manager.getEpic(1).getDuration(), "Duration of the added epic wasn't cleared!");
    }

    /**
     * Добавление нового эпика с указанием неверного статуса в отсутсвие подзадач.
     */
    @Test
    void whenTryToAddNewEpicWithWrongStatusItShouldBeNewWhenThereAreNoSubtasks() {
        List<Task> addingTasks = createSomeTasks();
        Epic epic = (Epic) addingTasks.get(1);
        epic.setStatus(Status.IN_PROGRESS);
        manager.addNewEpic(epic);
        assertEquals(Status.NEW, manager.getEpic(1).getStatus(), "Status wasn't updated!");
    }

    /**
     * Рассчет статуса эпика (все подзадачи со статусом NEW).
     */
    @Test
    void whenAllSubtasksOfAnEpicAreNewEpicStatusShouldBeNew() {
        List<Task> addingTasks = createSomeTasks();
        Subtask subtask1 = new Subtask();
        Subtask subtask2 = new Subtask();
        Subtask subtask3 = new Subtask();
        subtask1.setEpicId(1);
        subtask2.setEpicId(1);
        subtask3.setEpicId(1);
        subtask1.setStatus(Status.NEW);
        subtask2.setStatus(Status.NEW);
        subtask3.setStatus(Status.NEW);
        manager.addNewEpic((Epic) addingTasks.get(1));
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);
        manager.addNewSubtask(subtask3);
        assertEquals(Status.NEW, manager.getEpic(1).getStatus(), "Epic status is not NEW!");
    }

    /**
     * Рассчет статуса эпика (все подзадачи со статусом DONE).
     */
    @Test
    void whenAllSubtasksOfAnEpicAreDoneEpicStatusShouldBeDone() {
        List<Task> addingTasks = createSomeTasks();
        Subtask subtask1 = new Subtask();
        Subtask subtask2 = new Subtask();
        Subtask subtask3 = new Subtask();
        subtask1.setEpicId(1);
        subtask2.setEpicId(1);
        subtask3.setEpicId(1);
        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        subtask3.setStatus(Status.DONE);
        manager.addNewEpic((Epic) addingTasks.get(1));
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);
        manager.addNewSubtask(subtask3);
        assertEquals(Status.DONE, manager.getEpic(1).getStatus(), "Epic status is not DONE!");
    }

    /**
     * Рассчет статуса эпика (подзадачи со статусами NEW, IN_PROGRESS и DONE).
     */
    @Test
    void whenSubtasksOfAnEpicAreNewInProgressDoneEpicStatusShouldBeInProgress() {
        List<Task> addingTasks = createSomeTasks();
        Subtask subtask1 = new Subtask();
        Subtask subtask2 = new Subtask();
        Subtask subtask3 = new Subtask();
        subtask1.setEpicId(1);
        subtask2.setEpicId(1);
        subtask3.setEpicId(1);
        subtask1.setStatus(Status.NEW);
        subtask2.setStatus(Status.IN_PROGRESS);
        subtask3.setStatus(Status.DONE);
        manager.addNewEpic((Epic) addingTasks.get(1));
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);
        manager.addNewSubtask(subtask3);
        assertEquals(Status.IN_PROGRESS, manager.getEpic(1).getStatus(), "Epic status is not IN_PROGRESS!");
    }

    /**
     * Расчет длительности, времени старта и окончания эпика по подзадачам
     */
    @Test
    void whenAddingSubtasksEpicTemporalFieldsAndDurationShouldChange() {
        List<Task> addingTasks = createSomeTasks();
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
        manager.addNewEpic((Epic) addingTasks.get(1));
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);
        manager.addNewSubtask(subtask3);
        assertEquals(manager.getSubtask(2).getStartTime(), manager.getEpic(1).getStartTime(),
                "Received epic has wrong start time!");
        assertEquals(60, manager.getEpic(1).getDuration(), "Received epic has wrong duration!");
        assertEquals(manager.getSubtask(4).getEndTime(), manager.getEpic(1).getEndTime(),
                "Received epic has wrong end time!");
    }

    /**
     * Перерасчет длительности, времени старта и окончания эпика после удаления подзадачам
     */
    @Test
    void whenDeleteASubtaskOfAnEpicTemporalFieldsAndDurationShouldChange() {
        List<Task> addingTasks = createSomeTasks();
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
        manager.addNewEpic((Epic) addingTasks.get(1));
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);
        manager.addNewSubtask(subtask3);
        manager.deleteTaskById(2);
        assertEquals(manager.getSubtask(3).getStartTime(), manager.getEpic(1).getStartTime(),
                "Received epic has wrong start time!");
        assertEquals(55, manager.getEpic(1).getDuration(), "Received epic has wrong duration!");
        assertEquals(manager.getSubtask(4).getEndTime(), manager.getEpic(1).getEndTime(),
                "Received epic has wrong end time!");
    }

    /**
     * Перерасчет статуса эпика после удаления подзадачи
     */
    @Test
    void whenDeleteASubtaskOfAnEpicItShouldChangeStatus() {
        List<Task> addingTasks = createSomeTasks();
        Subtask subtask = new Subtask();
        subtask.setEpicId(1);
        subtask.setStatus(Status.IN_PROGRESS);
        manager.addNewEpic((Epic) addingTasks.get(1));
        manager.addNewSubtask(subtask);
        manager.deleteTaskById(2);
        assertEquals(Status.NEW, manager.getEpic(1).getStatus(), "Epic status wasn't changed!");
    }

    /**
     * Обновление подзадачи со стандартным поведением.
     */
    @Test
    void updateSubtask() {
        List<Task> addingTasks = createSomeTasks();
        manager.addNewEpic((Epic) addingTasks.get(1));
        Subtask subtask = ((Subtask) addingTasks.get(2));
        subtask.setEpicId(1);
        manager.addNewSubtask(subtask);
        Subtask newSubtask = new Subtask();
        newSubtask.setId(2);
        newSubtask.setEpicId(1);
        newSubtask.setTitle("Updated_Title");
        newSubtask.setDescription("Updated_Description");
        newSubtask.setStatus(Status.DONE);
        newSubtask.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 01:00", DT_FORMATTER), ZONE_ID));
        newSubtask.setDuration(20);
        manager.updateSubtask(newSubtask);
        assertEquals(newSubtask, manager.getSubtask(2), "Updated subtask is not equal to received subtask!");
    }

    /**
     * Обновление подзадачи с пересекающимся временем исполнения существующей задачи.
     */
    @Test
    void whenTryToUpdateSubtaskWithIntersectedRuntimeOfAnExistingTaskShouldBeThrownAnException() {
        List<Task> addingTasks = createSomeTasks();
        manager.addNewTask(addingTasks.get(0));
        manager.addNewEpic((Epic) addingTasks.get(1));
        manager.addNewSubtask((Subtask) addingTasks.get(2));
        Subtask subtask = (Subtask) addingTasks.get(2);
        subtask.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:00", DT_FORMATTER), ZONE_ID));
        String exceptionMessage = null;
        try {
            manager.updateSubtask(subtask);
        } catch (AddingAndUpdatingException |
                 NoSuchTaskException e) {
            exceptionMessage = e.getMessage();
        }
        assertEquals("Updated subtask intersects in execution time with an existing task!", exceptionMessage,
                "Updating subtask was updated despite intersecting the existing task!");

    }

    /**
     * Обновление подзадачи без указания id.
     */
    @Test
    void whenTryToUpdateSubtaskWithNoIdShouldBeThrownAnException() {
        List<Task> addingTasks = createSomeTasks();
        manager.addNewTask(addingTasks.get(0));
        manager.addNewEpic((Epic) addingTasks.get(1));
        manager.addNewSubtask((Subtask) addingTasks.get(2));
        Subtask subtask = (Subtask) addingTasks.get(2);
        subtask.setId(0);
        String exceptionMessage = null;
        try {
            manager.updateSubtask(subtask);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            exceptionMessage = e.getMessage();
        }
        assertEquals("Task has no id!", exceptionMessage,
                "Exception wasn't thrown!");
    }

    /**
     * Добавление новой подзадачи со стандартным поведением.
     */
    @Test
    void addNewSubtask() {
        List<Task> addingTasks = createSomeTasks();
        manager.addNewTask(addingTasks.get(0));
        manager.addNewEpic((Epic) addingTasks.get(1));
        manager.addNewSubtask((Subtask) addingTasks.get(2));
        assertEquals(addingTasks.get(2), manager.getSubtask(3), "Subtask wasn't added!");
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
        manager.addNewTask(task);
        String exceptionMessage = null;
        try {
            manager.addNewSubtask(subtask);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            exceptionMessage = e.getMessage();
        }
        assertEquals("The adding task intersects in execution time with an existing task!", exceptionMessage,
                "Adding subtask was added despite intersecting the existing task!");
    }

    /**
     * Получение списка всех элементов о стандартным поведением.
     */
    @Test
    void getAllItems() {
        List<Task> addingTasks = createSomeTasks();
        manager.addNewTask(addingTasks.get(0));
        manager.addNewEpic((Epic) addingTasks.get(1));
        manager.addNewSubtask((Subtask) addingTasks.get(2));
        assertEquals(addingTasks, manager.getAllItems(), "Added tasks are not equal to received tasks!");
    }

    /**
     * Получение списка всех элементов в отсутствие задач.
     */
    @Test
    void whenTryToGetAllItemsInTheirAbsenceShouldReceiveAnEmptyList() {
        assertTrue(manager.getAllItems().isEmpty(), "Received no existing tasks!");
    }

    /**
     * Получение списка всех задач о стандартным поведением.
     */
    @Test
    void getAllTasks() {
        List<Task> addingTasks = createSomeTasks();
        Task task1 = new Task();
        task1.setStatus(Status.NEW);
        Task task2 = new Task();
        task2.setStatus(Status.NEW);
        manager.addNewTask(addingTasks.get(0));
        manager.addNewEpic((Epic) addingTasks.get(1));
        manager.addNewSubtask((Subtask) addingTasks.get(2));
        manager.addNewTask(task1);
        manager.addNewTask(task2);
        List<Task> controllTaskList = List.of(addingTasks.get(0), task1, task2);
        assertEquals(controllTaskList, manager.getAllTasks(), "Received tasks are not equal to added tasks!");
    }

    /**
     * Получение списка всех задач в отсутствие задач.
     */
    @Test
    void whenTryToGetAllTasksInTheirAbsenceShouldReceiveAnEmptyList() {
        assertTrue(manager.getAllTasks().isEmpty(), "Received no existing tasks!");
    }

    /**
     * Получение списка эпиков о стандартным поведением.
     */
    @Test
    void getAllEpics() {
        List<Task> addingTasks = createSomeTasks();
        Epic epic1 = new Epic();
        epic1.setStatus(Status.NEW);
        Epic epic2 = new Epic();
        epic2.setStatus(Status.NEW);
        manager.addNewTask(addingTasks.get(0));
        manager.addNewEpic((Epic) addingTasks.get(1));
        manager.addNewSubtask((Subtask) addingTasks.get(2));
        manager.addNewEpic(epic1);
        manager.addNewEpic(epic2);
        List<Task> controllTaskList = List.of(addingTasks.get(1), epic1, epic2);
        assertEquals(controllTaskList, manager.getAllEpics(), "Received tasks are not equal to added tasks!");
    }

    /**
     * Получение списка эпиков в отсутствие эпиков.
     */
    @Test
    void whenTryToGetAllEpicsInTheirAbsenceShouldReceiveAnEmptyList() {
        assertTrue(manager.getAllEpics().isEmpty(), "Received no existing epics!");
    }

    /**
     * Получение списка подзадач о стандартным поведением.
     */
    @Test
    void getAllSubtasks() {
        List<Task> addingTasks = createSomeTasks();
        Subtask subtask1 = new Subtask();
        subtask1.setStatus(Status.NEW);
        subtask1.setEpicId(2);
        Subtask subtask2 = new Subtask();
        subtask2.setEpicId(2);
        subtask2.setStatus(Status.NEW);
        manager.addNewTask(addingTasks.get(0));
        manager.addNewEpic((Epic) addingTasks.get(1));
        manager.addNewSubtask((Subtask) addingTasks.get(2));
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);
        List<Task> controllTaskList = List.of(addingTasks.get(2), subtask1, subtask2);
        assertEquals(controllTaskList, manager.getAllSubtasks(), "Received tasks are not equal to added tasks!");
    }

    /**
     * Получение списка задач по типу со стандартным поведением.
     */
    @Test
    void getTasksByType() {
        List<Task> addingTasks = createSomeTasks();
        manager.addNewTask(addingTasks.get(0));
        manager.addNewEpic((Epic) addingTasks.get(1));
        manager.addNewSubtask((Subtask) addingTasks.get(2));
        assertEquals(addingTasks.get(0), manager.getTasksByType(Type.TASK).get(0),
                "Received task list is not equal to added task list!");
        assertEquals(addingTasks.get(1), manager.getTasksByType(Type.EPIC).get(0),
                "Received epic list is not equal to added epic list!");
        assertEquals(addingTasks.get(2), manager.getTasksByType(Type.SUBTASK).get(0),
                "Received subtask list is not equal to added subtask list!");
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
        List<Task> addingTasks = createSomeTasks();
        manager.addNewTask(addingTasks.get(0));
        manager.addNewEpic((Epic) addingTasks.get(1));
        manager.addNewSubtask((Subtask) addingTasks.get(2));
        manager.deleteAllItems();
        assertTrue(manager.getAllItems().isEmpty(), "Deleting was not successful!");
    }

    /**
     * Удаление задачи по id со стандартным поведением
     */
    @Test
    void deleteTaskById() {
        List<Task> addingTasks = createSomeTasks();
        manager.addNewTask(addingTasks.get(0));
        manager.addNewEpic((Epic) addingTasks.get(1));
        manager.addNewSubtask((Subtask) addingTasks.get(2));
        manager.deleteTaskById(1);
        String exceptionMessage = null;
        try {
            manager.getTaskById(1);
        } catch (NoSuchTaskException e) {
            exceptionMessage = e.getMessage();
        }
        assertEquals("There's no task with such id!", exceptionMessage, "Deleting was not successful!");
    }

    /**
     * Удаление задачи по id в отсутствие задачи
     */
    @Test
    void whenTryToDeleteNoExistingTaskByIdShouldBeThrownAnException() {
        String exceptionMessage = null;
        try {
            manager.getTaskById(666);
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
        List<Task> addingTasks = createSomeTasks();
        manager.addNewTask(addingTasks.get(0));
        manager.addNewEpic((Epic) addingTasks.get(1));
        manager.addNewSubtask((Subtask) addingTasks.get(2));
        manager.deleteTasksByType(Type.TASK);
        manager.deleteTasksByType(Type.SUBTASK);
        manager.deleteTasksByType(Type.EPIC);
        assertTrue(manager.getTasksByType(Type.TASK).isEmpty(), "Received task list is not empty!");
        assertTrue(manager.getTasksByType(Type.EPIC).isEmpty(), "Received epic list is not empty!");
        assertTrue(manager.getTasksByType(Type.SUBTASK).isEmpty(), "Received subtask list is not empty!");
    }

    /**
     * Получение задачи по id со стандартным поведением.
     */
    @Test
    void getTaskById() {
        List<Task> addingTasks = createSomeTasks();
        manager.addNewTask(addingTasks.get(0));
        manager.addNewEpic((Epic) addingTasks.get(1));
        manager.addNewSubtask((Subtask) addingTasks.get(2));
        assertEquals(addingTasks.get(0), manager.getTaskById(1), "Received task is not equal to added task");
        assertEquals(addingTasks.get(1), manager.getTaskById(2), "Received task is not equal to added task");
        assertEquals(addingTasks.get(2), manager.getTaskById(3), "Received task is not equal to added task");
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
        List<Task> addingTasks = createSomeTasks();
        Subtask subtask1 = new Subtask();
        subtask1.setStatus(Status.NEW);
        subtask1.setEpicId(2);
        Subtask subtask2 = new Subtask();
        subtask2.setEpicId(2);
        subtask2.setStatus(Status.NEW);
        manager.addNewTask(addingTasks.get(0));
        manager.addNewEpic((Epic) addingTasks.get(1));
        manager.addNewSubtask((Subtask) addingTasks.get(2));
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);
        List<Task> comtrollList = List.of(addingTasks.get(2), subtask1, subtask2);
        assertEquals(comtrollList, manager.getEpicsSubtasksById(2), "Received list is not equal to added list");
    }

    /**
     * Получение списка подзадач эпика в отсустсвие подзадач.
     */
    @Test
    void whenTryToGetSubtaskListOfAnEpicWithoutSubtasksShouldBeThrownAnException() {
        List<Task> addingTasks = createSomeTasks();
        manager.addNewEpic((Epic) addingTasks.get(1));
        String exceptionMessage = null;
        try {
            manager.getEpicsSubtasksById(1);
        } catch (NoSuchTaskException e) {
            exceptionMessage = e.getMessage();
        }
        assertEquals("Epic has no subtasks!", exceptionMessage, "Exception wasn't thrown!");
    }

    /**
     * Получение списка приоритетных задач со стандартным поведением.
     */
    @Test
    void getPrioritizedTasks() {
        List<Task> addingTasks = createSomeTasks();
        manager.addNewTask(addingTasks.get(0));
        manager.addNewEpic((Epic) addingTasks.get(1));
        manager.addNewSubtask((Subtask) addingTasks.get(2));
        List<Task> controllList = List.of(addingTasks.get(0), addingTasks.get(2));
        assertEquals(controllList, manager.getPrioritizedTasks(), "Received tasks are in wrong order!");
    }

    /**
     * Получение списка приоритетных задач в отсутствие задач.
     */
    @Test
    void whenTryToGetPrioritizedTasksInTheirAbsenceShouldReceiveAnEmptyList() {
        assertTrue(manager.getPrioritizedTasks().isEmpty(), "Received wrong priority list!");
    }

    List<Task> createSomeTasks() {
        Task task = new Task();
        task.setStatus(Status.NEW);
        task.setTitle("Test_title");
        task.setDescription("Test_description");
        task.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:00", DT_FORMATTER), ZONE_ID));
        task.setDuration(10L);

        Epic epic = new Epic();
        epic.setStatus(Status.NEW);
        epic.setTitle("Test_title");
        epic.setDescription("Test_description");

        Subtask subtask = new Subtask();
        subtask.setStatus(Status.NEW);
        subtask.setEpicId(2);
        subtask.setTitle("Test_title");
        subtask.setDescription("Test_description");
        subtask.setStartTime(ZonedDateTime.of(LocalDateTime.parse("01.01.2023 00:10", DT_FORMATTER), ZONE_ID));
        subtask.setDuration(10L);

        List<Task> taskList = new ArrayList<>();
        taskList.add(task);
        taskList.add(epic);
        taskList.add(subtask);

        return taskList;
    }

}