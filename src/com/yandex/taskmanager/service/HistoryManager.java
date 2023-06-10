package com.yandex.taskmanager.service;

import com.yandex.taskmanager.model.Task;

import java.util.LinkedList;

public interface HistoryManager {

    LinkedList<Task> getHistory();

    void add(Task task);
}
