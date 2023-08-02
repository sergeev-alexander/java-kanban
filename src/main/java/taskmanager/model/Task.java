package taskmanager.model;

import java.time.ZonedDateTime;
import java.util.Objects;

public class Task {

    private int id;
    private Status status;
    private String title;
    private String description;
    private ZonedDateTime startTime;
    private long duration;


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

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public ZonedDateTime getEndTime() {
        if (startTime != null) {
            return startTime.plusMinutes(duration);
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return id == task.id
                && duration == task.duration
                && status == task.status
                && Objects.equals(title, task.title)
                && Objects.equals(description, task.description)
                && Objects.equals(startTime, task.startTime)
                && Objects.equals(getEndTime(), task.getEndTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status, title, description);
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s\n",
                getId(),
                getType(),
                getTitle(),
                getStatus(),
                getDescription(),
                getStartTime(),
                getDuration(),
                getEndTime());
    }

}
