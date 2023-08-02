package taskmanager.service;

import taskmanager.exceptions.AddingAndUpdatingException;
import taskmanager.exceptions.NoSuchTaskException;
import taskmanager.model.Epic;
import taskmanager.model.Subtask;
import taskmanager.model.Task;
import taskmanager.model.Type;

import java.util.List;

public interface TaskManager {

    List<Task> getHistory();

    Task getTask(int taskId) throws NoSuchTaskException;

    Epic getEpic(int epicId) throws NoSuchTaskException;

    Subtask getSubtask(int subtaskId) throws NoSuchTaskException;

    void updateTask(Task newTask) throws AddingAndUpdatingException, NoSuchTaskException;

    void addNewTask(Task task) throws AddingAndUpdatingException, NoSuchTaskException;

    void updateEpic(Epic newEpic) throws AddingAndUpdatingException, NoSuchTaskException;

    void addNewEpic(Epic epic) throws AddingAndUpdatingException, NoSuchTaskException;

    void updateSubtask(Subtask newSubtask) throws AddingAndUpdatingException, NoSuchTaskException;

    void addNewSubtask(Subtask subtask) throws AddingAndUpdatingException, NoSuchTaskException;

    List<Task> getAllItems();

    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasks();

    List<Task> getTasksByType(Type type);

    void deleteAllItems();

    void deleteTaskById(int id) throws NoSuchTaskException;

    void deleteTasksByType(Type type) throws NoSuchTaskException;

    Task getTaskById(int id) throws NoSuchTaskException;

    List<Subtask> getEpicsSubtasksById(int epicId) throws NoSuchTaskException;

    List<Task> getPrioritizedTasks();

}
