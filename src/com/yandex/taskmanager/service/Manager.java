package com.yandex.taskmanager.service;

import com.yandex.taskmanager.model.*;

import java.util.*;

public class Manager {

    private int id = 0;
    private final Map<Integer, Task> taskMap = new HashMap<>();
    private final Map<Integer, Epic> epicMap = new HashMap<>();
    private final Map<Integer, Subtask> subtaskMap = new HashMap<>();

    public int getId() { // Согласно предыдущему ревью метод createId() был сделан приватным, и чтобы получить
        // доступ к нему из класса Interaction был создан публичный метод getId()
        return createId();
    }

    public void updateTask(Task newTask) {
        Task existingTask = taskMap.get(newTask.getId());
        existingTask.setTitle(newTask.getTitle());
        existingTask.setDescription(newTask.getDescription());
    }

    public void addNewTask(Task task) {
        taskMap.put(task.getId(), task);
    }

    public void updateEpic(Epic newEpic) {
        Epic existingEpic = epicMap.get(newEpic.getId());
        existingEpic.setTitle(newEpic.getTitle());
        existingEpic.setDescription(newEpic.getDescription());
    }

    public void addNewEpic(Epic epic) {
        epicMap.put(epic.getId(), epic);
    }

    public void updateSubtask(Subtask newSubtask) {
        Subtask exsistingSubtask = subtaskMap.get(newSubtask.getId());
        exsistingSubtask.setTitle(exsistingSubtask.getTitle());
        exsistingSubtask.setDescription(newSubtask.getDescription());
    }

    public void addNewSubtask(Subtask subtask) {
        subtaskMap.put(subtask.getId(), subtask);
        epicMap.get(subtask.getEpicId()).addSubtasksIdToEpicList(subtask.getId());
        updateEpicStatus(subtask.getEpicId());
    }

    public List<Task> getAllItems() {
        ArrayList<Task> list = new ArrayList<>();
        list.addAll(taskMap.values());
        list.addAll(epicMap.values());
        list.addAll(subtaskMap.values());
        return list;
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(taskMap.values());
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<>(epicMap.values());
    }

    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtaskMap.values());
    }

    public List<Task> getTasksByType(Type type) {
        List<Task> list = new ArrayList<>();
        switch (type) {
            case TASK:
                list.addAll(taskMap.values());
                return list;
            case EPIC:
                list.addAll(epicMap.values());
                return list;
            case SUBTASK:
                list.addAll(subtaskMap.values());
                return list;
            default:
                return null;
        }
    }

    public void deleteAllTasks() {
        taskMap.clear();
        epicMap.clear();
        subtaskMap.clear();
    }

    public void deleteTaskById(int id) {
        if (taskMap.containsKey(id)) {
            taskMap.remove(id);
        } else if (epicMap.containsKey(id)) {
            List<Integer> subTasksIdList = epicMap.get(id).getSubTasksIdList();
            if (!subTasksIdList.isEmpty()) {
                for (int subtaskId : subTasksIdList) {
                    subtaskMap.remove(subtaskId);
                }
            }
            epicMap.remove(id, epicMap.get(id));
        } else if (subtaskMap.containsKey(id)) {
            int epicId = subtaskMap.get(id).getEpicId();
            subtaskMap.remove(id);
            epicMap.get(epicId).removeSubtaskIdFromEpicList(id);
            updateEpicStatus(epicId);
        }
    }

    public void deleteTasksByType(Type type) {
        switch (type) {
            case TASK:
                taskMap.clear();
                break;
            case EPIC:
                epicMap.clear();
                subtaskMap.clear();
                break;
            case SUBTASK:
                subtaskMap.clear();
                for (Epic epic : epicMap.values()) {
                    epic.getSubTasksIdList().clear();
                    updateEpicStatus(epic.getId());
                }
                break;
        }
    }

    public Task getTaskById(int id) {
        if (taskMap.containsKey(id)) {
            return taskMap.get(id);
        } else if (epicMap.containsKey(id)) {
            return epicMap.get(id);
        } else if (subtaskMap.containsKey(id)) {
            return subtaskMap.get(id);
        }
        return null;
    }

    public List<Subtask> getEpicsSubtasksById(int epicId) {
        List<Integer> subtasksIdList = epicMap.get(epicId).getSubTasksIdList();
        List<Subtask> subtaskList = new ArrayList<>();
        for (int subtaskId : subtasksIdList) {
            subtaskList.add(subtaskMap.get(subtaskId));
        }
        return subtaskList;
    }

    private int createId() {
        return ++id;
    }

    private void updateEpicStatus(int epicId) {
        Epic existingEpic = epicMap.get(epicId);
        List<Integer> subtasksIdList = existingEpic.getSubTasksIdList();
        int newSubtasksCount = 0;
        int doneSubtasksCount = 0;
        if (!subtasksIdList.isEmpty() && !subtaskMap.isEmpty()) {
            for (int subtaskId : subtasksIdList) {
                if (subtaskMap.get(subtaskId).getStatus() == Status.NEW) {
                    newSubtasksCount++;
                } else if (subtaskMap.get(subtaskId).getStatus() == Status.DONE) {
                    doneSubtasksCount++;
                } else {
                    existingEpic.setStatus(Status.IN_PROGRESS);
                    return;
                }
            }
            if (newSubtasksCount == subtasksIdList.size()) {
                existingEpic.setStatus(Status.NEW);
            } else if (doneSubtasksCount == subtasksIdList.size()) {
                existingEpic.setStatus(Status.DONE);
            }
        } else {
            epicMap.get(epicId).setStatus(Status.NEW);
        }
    }
}