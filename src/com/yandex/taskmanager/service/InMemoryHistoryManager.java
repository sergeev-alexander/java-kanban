package com.yandex.taskmanager.service;

import com.yandex.taskmanager.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private Node first;
    private Node last;
    private int size = 0;

    private final Map<Integer, Node> map = new HashMap<>();

    @Override
    public List<Task> getHistory() {
        List<Task> list = new ArrayList<>();
        Node node = last;
        for (int i = 0; i < size; i++) {
            list.add(node.value);
            node = node.previous;
        }
        return list;
    }

    @Override
    public void add(Task task) {
        if (map.containsKey(task.getId())) {
            remove(task.getId());
        }
        if (size == 0) {
            Node node = new Node(null, task, null);
            first = node;
            last = node;
            map.put(task.getId(), node);
        } else {
            Node newPreviousNode = last;
            last = new Node(newPreviousNode, task, null);
            newPreviousNode.next = last;
            map.put(task.getId(), last);
        }
        size++;
    }

    @Override
    public void remove(int id) {
        if (!map.containsKey(id)) {
            return;
        }
        Node deletingNode = map.get(id);
        Node previousNode = deletingNode.previous;
        Node nextNode = deletingNode.next;
        if (nextNode != null) {
            nextNode.previous = previousNode;
        } else {
            last = previousNode;
        }
        if (previousNode != null) {
            previousNode.next = nextNode;
        } else {
            first = nextNode;
        }
        map.remove(id);
        size--;
    }

    public void clear() {
        first = null;
        last = null;
        size = 0;
        map.clear();
    }
}

