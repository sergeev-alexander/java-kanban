package taskmanager.service;

import taskmanager.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private Node first;
    private Node last;

    private final Map<Integer, Node> historyViewsMap = new HashMap<>();

    @Override
    public List<Task> getHistory() {
        List<Task> list = new ArrayList<>(historyViewsMap.size());
        Node node = last;
        while (node != null) {
            list.add(node.value);
            node = node.previous;
        }
        return list;
    }

    @Override
    public void add(Task task) {
        remove(task.getId());
        if (historyViewsMap.size() == 0) {
            Node node = new Node(null, task, null);
            first = node;
            last = node;
        } else {
            Node newPreviousNode = last;
            last = new Node(newPreviousNode, task, null);
            newPreviousNode.next = last;
        }
        historyViewsMap.put(task.getId(), last);
    }

    @Override
    public void remove(int id) {
        Node deletingNode = historyViewsMap.remove(id);
        if (deletingNode == null) {
            return;
        }
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
    }

    public void clear() {
        first = null;
        last = null;
        historyViewsMap.clear();
    }

    private static class Node {

        private Node previous;
        private final Task value;
        private Node next;

        private Node(Node previous, Task value, Node next) {
            this.previous = previous;
            this.value = value;
            this.next = next;
        }
    }
}

