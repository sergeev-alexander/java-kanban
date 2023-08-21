package taskmanager.service;

import taskmanager.model.Epic;
import taskmanager.model.Subtask;
import taskmanager.model.Task;
import taskmanager.model.Type;

import java.util.List;

public interface TaskManager {

    List<Task> getHistory();

    Task getTask(int taskId);

    Epic getEpic(int epicId);

    Subtask getSubtask(int subtaskId);

    void updateTask(Task newTask);

    void addNewTask(Task task);

    void updateEpic(Epic newEpic);

    void addNewEpic(Epic epic);

    void updateSubtask(Subtask newSubtask);

    void addNewSubtask(Subtask subtask);

    List<Task> getAllItems();

    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasks();

    List<Task> getTasksByType(Type type);

    void deleteAllItems();

    void deleteTaskById(int id);

    void deleteTasksByType(Type type);

    Task getTaskById(int id);

    List<Subtask> getEpicsSubtasksById(int epicId);

    List<Task> getPrioritizedTasks();

    int getIdField();

    void stopServer();

}
