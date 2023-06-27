package com.yandex.taskmanager.service;

import com.yandex.taskmanager.model.*;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private int id = 0;
    private final Map<Integer, Task> taskMap = new HashMap<>();
    private final Map<Integer, Epic> epicMap = new HashMap<>();
    private final Map<Integer, Subtask> subtaskMap = new HashMap<>();

    private final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public Task getTask(int taskId) {
        Task task = taskMap.get(taskId);
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int epicId) {
        Epic epic = epicMap.get(epicId);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int subtaskId) {
        Subtask subtask = subtaskMap.get(subtaskId);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public void updateTask(Task newTask) {
        Task existingTask = taskMap.get(newTask.getId());
        existingTask.setTitle(newTask.getTitle());
        existingTask.setDescription(newTask.getDescription());
        existingTask.setStatus(newTask.getStatus());
    }

    @Override
    public void addNewTask(Task task) {
        task.setId(createId());
        taskMap.put(task.getId(), task);
    }

    @Override
    public void updateEpic(Epic newEpic) {
        Epic existingEpic = epicMap.get(newEpic.getId());
        existingEpic.setTitle(newEpic.getTitle());
        existingEpic.setDescription(newEpic.getDescription());
    }

    @Override
    public void addNewEpic(Epic epic) {
        epic.setId(createId());
        epicMap.put(epic.getId(), epic);
    }

    @Override
    public void updateSubtask(Subtask newSubtask) {
        Subtask existingSubtask = subtaskMap.get(newSubtask.getId());
        existingSubtask.setTitle(existingSubtask.getTitle());
        existingSubtask.setDescription(newSubtask.getDescription());
        existingSubtask.setStatus(newSubtask.getStatus());
        updateEpicStatus(newSubtask.getEpicId());
    }

    @Override
    public void addNewSubtask(Subtask subtask) {
        subtask.setId(createId());
        subtaskMap.put(subtask.getId(), subtask);
        epicMap.get(subtask.getEpicId()).addSubtasksIdToEpicList(subtask.getId());
        updateEpicStatus(subtask.getEpicId());
    }

    @Override
    public List<Task> getAllItems() {
        ArrayList<Task> list = new ArrayList<>();
        list.addAll(taskMap.values());
        list.addAll(epicMap.values());
        list.addAll(subtaskMap.values());
        return list;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(taskMap.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epicMap.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtaskMap.values());
    }

    @Override
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

    @Override
    public void deleteAllTasks() {
        taskMap.clear();
        epicMap.clear();
        subtaskMap.clear();
        historyManager.clear();
    }

    @Override
    public void deleteTaskById(int id) {
        if (taskMap.containsKey(id)) {
            historyManager.remove(id);
            taskMap.remove(id);
        } else if (epicMap.containsKey(id)) {
            List<Integer> subTasksIdList = epicMap.get(id).getSubTasksIdList();
            if (!subTasksIdList.isEmpty()) {
                for (int subtaskId : subTasksIdList) {
                    historyManager.remove(subtaskId);
                    subtaskMap.remove(subtaskId);
                }
            }
            epicMap.remove(id);
            historyManager.remove(id);
        } else if (subtaskMap.containsKey(id)) {
            int epicId = subtaskMap.remove(id).getEpicId();
            historyManager.remove(id);
            epicMap.get(epicId).removeSubtaskIdFromEpicList(id);
            updateEpicStatus(epicId);
        }
    }

    @Override
    public void deleteTasksByType(Type type) {
        switch (type) {
            case TASK:
                for (Task task : taskMap.values()) {
                    historyManager.remove(task.getId());
                }
                taskMap.clear();
                break;
            case EPIC:
                for (Task task : epicMap.values()) {
                    historyManager.remove(task.getId());
                }
                epicMap.clear();
                for (Task task : subtaskMap.values()) {
                    historyManager.remove(task.getId());
                }
                subtaskMap.clear();
                break;
            case SUBTASK:
                for (Task task : subtaskMap.values()) {
                    historyManager.remove(task.getId());
                }
                subtaskMap.clear();
                for (Epic epic : epicMap.values()) {
                    epic.getSubTasksIdList().clear();
                    updateEpicStatus(epic.getId());
                }
                break;
        }
    }

    @Override
    public Task getTaskById(int id) {
        if (taskMap.containsKey(id)) {
            return getTask(id);
        } else if (epicMap.containsKey(id)) {
            return getEpic(id);
        } else if (subtaskMap.containsKey(id)) {
            return getSubtask(id);
        }
        return null;
    }

    @Override
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
            } else {
                existingEpic.setStatus(Status.IN_PROGRESS);
            }
        } else {
            epicMap.get(epicId).setStatus(Status.NEW);
        }
    }
}