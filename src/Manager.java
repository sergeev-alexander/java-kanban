import java.util.*;

public class Manager {

    private int id = 0;
    private Map<Integer, Task> taskMap = new HashMap<>();

    public Map<Integer, Task> getTaskMap() {
        return taskMap;
    }

    public int createId() {
        return ++id;
    }

    public void updateTask(Task task) {
        taskMap.put(task.getId(), task);
        updateEpicStatuses();
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(taskMap.values());
    }

    public List<Task> getTasksByType(Type type) {
        List<Task> taskList = new ArrayList<>();
        for (Task task : taskMap.values()) {
            if (task.getType().equals(type)) {
                taskList.add(task);
            }
        }
        return taskList;
    }

    public List<Task> getTasksByStatus(Status status) {
        List<Task> taskList = new ArrayList<>();
        for (Task task : taskMap.values()) {
            if (task.getStatus().equals(status)) {
                taskList.add(task);
            }
        }
        return taskList;
    }

    public void deleteAllTasks() {
        taskMap.clear();
    }

    public void deleteTaskById(int id) {
        taskMap.remove(id);
        updateEpicStatuses();
    }

    public void deleteTasksByType(Type type) {
        List<Integer> idList = new ArrayList<>();
        for (Task task : taskMap.values()) {
            if (task.getType().equals(type)) {
                idList.add(task.getId());
            }
        }
        for (int id : idList) {
            taskMap.remove(id);
        }
        updateEpicStatuses();
    }

    public Task getTaskById(int id) {
        return taskMap.get(id);
    }

    public List<Task> getEpicsSubtasksById(int epicId) {
        List<Integer> subtasksIdList = ((Epic) taskMap.get(epicId)).getSubTasksIdList();
        List<Task> subtaskList = new ArrayList<>();
        for (int subtaskId : subtasksIdList) {
            subtaskList.add(taskMap.get(subtaskId));
        }
        return subtaskList;
    }

    public void updateEpicStatuses() {
        if (!taskMap.isEmpty()) {
            List<Task> epics = getTasksByType(Type.EPIC);
            for (Task epic : epics) {
                List<Integer> subtaskIdList = ((Epic) epic).getSubTasksIdList();
                if (subtaskIdList.size() == 0) {
                    epic.setStatus(Status.NEW);
                } else {
                    int newSubtasksCount = 0;
                    int doneSubtasksCount = 0;
                    for (int i = 0; i < subtaskIdList.size(); i++) {
                        Integer subtask = subtaskIdList.get(i);
                        if (!taskMap.containsKey(subtask)) {
                            ((Epic) epic).getSubTasksIdList().remove(subtask);
                            updateEpicStatuses();
                        } else {
                            if (taskMap.get(subtask).getStatus().equals(Status.NEW)) {
                                newSubtasksCount++;
                            } else if (taskMap.get(subtask).getStatus().equals(Status.DONE)) {
                                doneSubtasksCount++;
                            }
                            if (newSubtasksCount == ((Epic) epic).getSubTasksIdList().size()) {
                                epic.setStatus(Status.NEW);
                            } else if (doneSubtasksCount == ((Epic) epic).getSubTasksIdList().size()) {
                                epic.setStatus(Status.DONE);
                            } else {
                                epic.setStatus(Status.IN_PROGRESS);
                            }
                        }
                    }
                }
            }
        }
    }
}