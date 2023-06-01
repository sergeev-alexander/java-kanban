import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {

    private final Type type = Type.EPIC;
    private List<Integer> subTasksIdList = new ArrayList<>();

    @Override
    public Type getType() {
        return this.type;
    }

    public List<Integer> getSubTasksIdList() {
        return subTasksIdList;
    }

    public void setSubTasksIdList(List<Integer> subTasksIdList) {
        this.subTasksIdList = subTasksIdList;
    }

    public void setSubtasksIdToEpicList(int subtasksId) {
        this.subTasksIdList.add(subtasksId);
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
        return "Epic{}" +
                "id=" + getId() +
                ", type=" + type +
                ", status='" + getStatus() + '\'' +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", subTasksIdList='" + subTasksIdList +
                "'}\n";
    }
}
