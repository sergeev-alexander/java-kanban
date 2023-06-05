package com.yandex.taskmanager.service;

import com.yandex.taskmanager.model.*;

import java.util.*;

public class Manager {

    private int id = 0;
    private Map<Integer, Task> taskMap = new HashMap<>();
    private Map<Integer, Epic> epicMap = new HashMap<>();
    private Map<Integer, Subtask> subtaskMap = new HashMap<>();

    public Manager(){
    }

    private Manager(Manager manager) {
        this.taskMap = manager.taskMap;
        this.epicMap = manager.epicMap;
        this.subtaskMap = manager.subtaskMap;
    }

    public Map<Integer, Task> getTaskMap() {
        Manager manager = new Manager(this);
        return manager.taskMap;
    }

    public Map<Integer, Epic> getEpicMap() {
        Manager manager = new Manager(this);
        return manager.epicMap;
    }

    public Map<Integer, Subtask> getSubtaskMap() {
        Manager manager = new Manager(this);
        return manager.subtaskMap;
    }

    private int createId() {
        return ++id;
    }

    public int getId() {
        return createId();
    }

    public void updateTask(Task task) {
        taskMap.put(task.getId(), task);
    }

    public void updateEpic(Epic epic) {
        epicMap.put(epic.getId(), epic);
    }

    public void updateSubtask(Subtask subtask) {
        subtaskMap.put(subtask.getId(), subtask);
        updateEpicSubtasksList(subtask.getEpicId());
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

    public Object getTasksByType(Type type) {
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
            taskMap.remove(id, taskMap.get(id));
        } else if (epicMap.containsKey(id)) {
            List<Integer> subTasksIdList = epicMap.get(id).getSubTasksIdList();
            if (!subTasksIdList.isEmpty()) {
                for (int subtaskId : subTasksIdList) {
                    subtaskMap.remove(subtaskId, subtaskMap.get(subtaskId));
                }
            }
            epicMap.remove(id, epicMap.get(id));
        } else if (subtaskMap.containsKey(id)) {
            int epicId = subtaskMap.get(id).getEpicId();
            subtaskMap.remove(id, subtaskMap.get(id));
            updateEpicSubtasksList(epicId);
            updateEpicStatus(epicId);
        }
    }

    public void deleteTasksByType(Type type) {
        switch (type) {
            case TASK:
                taskMap.clear();
                break;
            case EPIC:
                for (int epicId : epicMap.keySet()) {
                    List<Integer> subTasksIdList = epicMap.get(epicId).getSubTasksIdList();
                    if (!subTasksIdList.isEmpty()) {
                        for (int subtaskId : subTasksIdList) {
                            subtaskMap.remove(subtaskId, subtaskMap.get(subtaskId));
                        }
                    }
                }
                epicMap.clear();
                break;
            case SUBTASK:
                subtaskMap.clear();
                for (int epicId : epicMap.keySet()) {
                    updateEpicSubtasksList(epicId);
                    updateEpicStatus(epicId);
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

    private void updateEpicSubtasksList(int epicId) {
        List<Integer> subtasksIdList = epicMap.get(epicId).getSubTasksIdList();
        List<Integer> newSubtasksIdList = new ArrayList<>();
        for (int subtaskId : subtasksIdList) {
            if (subtaskMap.containsKey(subtaskId)) {
                newSubtasksIdList.add(subtaskId);
            }
        }
        epicMap.get(epicId).setSubTasksIdList(newSubtasksIdList);
    }

    private void updateEpicStatus(int epicId) {
        List<Integer> subtasksIdList = epicMap.get(epicId).getSubTasksIdList();
        int newSubtasksCount = 0;
        int doneSubtasksCount = 0;
        if (!subtasksIdList.isEmpty()) {
            for (int subtaskId : subtasksIdList) {
                if (subtaskMap.get(subtaskId).getStatus() == Status.NEW) {
                    newSubtasksCount++;
                } else if (subtaskMap.get(subtaskId).getStatus() == Status.DONE) {
                    doneSubtasksCount++;
                }
            }
            if (newSubtasksCount == subtasksIdList.size()) {
                epicMap.get(epicId).setStatus(Status.NEW);
            } else if (doneSubtasksCount == subtasksIdList.size()) {
                epicMap.get(epicId).setStatus(Status.DONE);
            } else {
                epicMap.get(epicId).setStatus(Status.IN_PROGRESS);
            }
        } else {
            epicMap.get(epicId).setStatus(Status.NEW);
        }
    }
}