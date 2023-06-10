package com.yandex.taskmanager.service;

import com.yandex.taskmanager.model.Task;

import java.util.LinkedList;

public class InMemoryHistoryManager implements HistoryManager {

    private final LinkedList<Task> historyList = new LinkedList<>();

    private final static int HISTORY_LIST_SIZE = 10;

    @Override
    public LinkedList<Task> getHistory() {
        return new LinkedList<>(historyList);
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        if (historyList.size() < HISTORY_LIST_SIZE) {
            historyList.addFirst(task);
        } else {
            historyList.addFirst(task);
            historyList.removeLast();
        }
    }
}
