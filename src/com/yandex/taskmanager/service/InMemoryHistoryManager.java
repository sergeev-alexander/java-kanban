package com.yandex.taskmanager.service;

import com.yandex.taskmanager.model.Task;

import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private final LinkedList<Task> historyList = new LinkedList<>();

    @Override
    public List<Task> getHistory() {
        return historyList;
    }

    @Override
    public void add(Task task) {
        if (historyList.size() < 10) {
            historyList.addFirst(task);
        } else {
            historyList.addFirst(task);
            historyList.removeLast();
        }
    }
}
