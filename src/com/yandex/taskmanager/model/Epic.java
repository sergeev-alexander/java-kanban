package com.yandex.taskmanager.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {

    private List<Integer> subTasksIdList = new ArrayList<>();

    @Override
    public Type getType() {
        return Type.EPIC;
    }

    public List<Integer> getSubTasksIdList() {
        return subTasksIdList;
    }

    public void setSubTasksIdList(List<Integer> subTasksIdList) {
        this.subTasksIdList = subTasksIdList;
    }

    public void addSubtasksIdToEpicList(int subtasksId) {
        this.subTasksIdList.add(subtasksId);
    }

    public void removeSubtaskIdFromEpicList(int subtasksId) {
        subTasksIdList.remove((Integer) subtasksId);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subTasksIdList, epic.subTasksIdList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subTasksIdList);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(String.valueOf(getId()) + ','
                + getType().toString() + ','
                + getTitle() + ','
                + getStatus().toString() + ','
                + getDescription() + ','
                + "-,");
        for (int subtask : subTasksIdList) {
            result.append(subtask).append(",");
        }
        result.append("\n");
        return String.valueOf(result);
    }
}
