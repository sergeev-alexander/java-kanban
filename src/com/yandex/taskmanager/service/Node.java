package com.yandex.taskmanager.service;

import com.yandex.taskmanager.model.Task;

public class Node {

    public Node previous = null;
    public Task value;
    public Node next = null;

    public Node(Node previous, Task value, Node next) {
        this.previous = previous;
        this.value = value;
        this.next = next;
    }
}
