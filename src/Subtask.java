import java.util.Objects;

public class Subtask extends Task {

    private final Type type = Type.SUBTASK;
    private int epicId;

    @Override
    public Type getType() {
        return this.type;
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
        if (o == null || getClass() != o.getClass()) return false;
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
        return "Subtask{}" +
                "id=" + getId() +
                ", type=" + type +
                ", status='" + getStatus() + '\'' +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", epicId='" + epicId +
                "'}\n";
    }
}
