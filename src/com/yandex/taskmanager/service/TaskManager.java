package com.yandex.taskmanager.service;

import com.yandex.taskmanager.model.*;

import java.util.LinkedList;
import java.util.List;

public interface TaskManager {

    LinkedList<Task> getHistory();
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

    void deleteAllTasks();

    void deleteTaskById(int id);

    void deleteTasksByType(Type type);

    Task getTaskById(int id);

    List<Subtask> getEpicsSubtasksById(int epicId);
}
