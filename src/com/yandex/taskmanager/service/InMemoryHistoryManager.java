package com.yandex.taskmanager.service;

import com.yandex.taskmanager.model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private final List<Task> historyList = new ArrayList<>();

    private final static int HISTORY_LIST_SIZE = 10;

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(historyList);
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        if (historyList.size() >= HISTORY_LIST_SIZE) {
            historyList.remove(9);
        }
        historyList.add(0, task);
    }
}
