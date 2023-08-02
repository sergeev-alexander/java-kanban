package taskmanager.model;

import java.util.Objects;

public class Subtask extends Task {

    private int epicId;

    @Override
    public Type getType() {
        return Type.SUBTASK;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subtask)) return false;
        if (!super.equals(o)) return false;
        Subtask subtask = (Subtask) o;
        return epicId == subtask.epicId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicId);
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                getId(),
                getType(),
                getTitle(),
                getStatus(),
                getDescription(),
                getStartTime(),
                getDuration(),
                getEndTime(),
                getEpicId());
    }

}
