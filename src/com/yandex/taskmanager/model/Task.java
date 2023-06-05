package com.yandex.taskmanager.model;

import java.util.Objects;

public class Task {

    private int id;
    private Status status;
    private String title;
    private String description;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Type getType() {
        return Type.TASK;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && Objects.equals(status, task.status)
                && Objects.equals(title, task.title)
                && Objects.equals(description, task.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status, title, description);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", type=" + getType() +
                ", status=" + status +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                "'}'\n";
    }
}