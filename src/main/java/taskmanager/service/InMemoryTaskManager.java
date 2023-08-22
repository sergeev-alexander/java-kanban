package taskmanager.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import taskmanager.exceptions.AddingAndUpdatingException;
import taskmanager.exceptions.NoSuchTaskException;
import taskmanager.model.*;

import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    protected int id = 1;

    protected final Set<Task> prioritySet = new TreeSet<>(Comparator.comparing
                    ((Task::getStartTime), Comparator.nullsFirst(Comparator.reverseOrder()))
            .thenComparing(Task::getId).reversed());

    protected final Map<Integer, Task> taskMap = new HashMap<>();
    protected final Map<Integer, Epic> epicMap = new HashMap<>();
    protected final Map<Integer, Subtask> subtaskMap = new HashMap<>();

    protected final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public int getIdField() {
        return id;
    }

    @Override
    public Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new AdapterLocalDateTime())
                .setPrettyPrinting()
                .create();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public Task getTask(int taskId) {
        idCheck(taskId);
        Task task = taskMap.get(taskId);
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int epicId) {
        idCheck(epicId);
        Epic epic = epicMap.get(epicId);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int subtaskId) {
        idCheck(subtaskId);
        Subtask subtask = subtaskMap.get(subtaskId);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public void addNewTask(Task task) {
        intersectingCheck(task);
        task.setId(createId());
        prioritySet.add(task);
        taskMap.put(task.getId(), task);
    }

    @Override
    public void addNewEpic(Epic epic) {
        epic.setId(createId());
        epic.setSubTasksIdList(new ArrayList<>());
        epic.setStartTime(null);
        epic.setDuration(0);
        epic.setEndTime(null);
        epicMap.put(epic.getId(), epic);
        updateEpicFields(epic.getId());
    }

    @Override
    public void addNewSubtask(Subtask subtask) {
        intersectingCheck(subtask);
        if (epicMap.isEmpty()) {
            throw new AddingAndUpdatingException("Unable to add subtask! There's no epics!");
        } else if (!epicMap.containsKey(subtask.getEpicId())) {
            throw new AddingAndUpdatingException("Unable to add subtask! There's no epics with such epicId");
        }
        subtask.setId(createId());
        prioritySet.add(subtask);
        subtaskMap.put(subtask.getId(), subtask);
        epicMap.get(subtask.getEpicId()).addSubtasksIdToEpicList(subtask.getId());
        updateEpicFields(subtask.getEpicId());
    }

    @Override
    public void updateTask(Task newTask) {
        idCheck(newTask.getId());
        Task existingTask = taskMap.get(newTask.getId());
        prioritySet.remove(existingTask);
        try {
            intersectingCheck(existingTask);
        } catch (AddingAndUpdatingException e) {
            prioritySet.add(existingTask);
            throw new AddingAndUpdatingException("Updated task intersects in execution time with an existing task!");
        }
        existingTask.setTitle(newTask.getTitle());
        existingTask.setDescription(newTask.getDescription());
        existingTask.setStatus(newTask.getStatus());
        existingTask.setStartTime(newTask.getStartTime());
        existingTask.setDuration(newTask.getDuration());
        prioritySet.add(existingTask);
    }

    @Override
    public void updateEpic(Epic newEpic) {
        idCheck(newEpic.getId());
        Epic existingEpic = epicMap.get(newEpic.getId());
        existingEpic.setSubTasksIdList(new ArrayList<>());
        existingEpic.setStartTime(null);
        existingEpic.setDuration(0);
        existingEpic.setEndTime(null);
        existingEpic.setTitle(newEpic.getTitle());
        existingEpic.setDescription(newEpic.getDescription());
        updateEpicFields(existingEpic.getId());
    }

    @Override
    public void updateSubtask(Subtask newSubtask) {
        idCheck(newSubtask.getId());
        Subtask existingSubtask = subtaskMap.get(newSubtask.getId());
        prioritySet.remove(existingSubtask);
        updateEpicFields(existingSubtask.getEpicId());
        try {
            intersectingCheck(newSubtask);
        } catch (AddingAndUpdatingException e) {
            prioritySet.add(existingSubtask);
            throw new AddingAndUpdatingException("Updated subtask intersects in execution time with an existing task!");
        }
        existingSubtask.setTitle(newSubtask.getTitle());
        existingSubtask.setDescription(newSubtask.getDescription());
        existingSubtask.setStatus(newSubtask.getStatus());
        existingSubtask.setStartTime(newSubtask.getStartTime());
        existingSubtask.setDuration(newSubtask.getDuration());
        prioritySet.add(existingSubtask);
        epicMap.get(existingSubtask.getEpicId()).addSubtasksIdToEpicList(existingSubtask.getId());
        updateEpicFields(existingSubtask.getEpicId());
    }

    @Override
    public List<Task> getAllItems() {
        ArrayList<Task> list = new ArrayList<>();
        list.addAll(taskMap.values());
        list.addAll(epicMap.values());
        list.addAll(subtaskMap.values());
        return list;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(taskMap.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epicMap.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtaskMap.values());
    }

    @Override
    public List<Task> getTasksByType(Type type) {
        List<Task> list = new ArrayList<>();
        switch (type) {
            case TASK:
                list.addAll(getAllTasks());
                return list;
            case EPIC:
                list.addAll(getAllEpics());
                return list;
            case SUBTASK:
                list.addAll(getAllSubtasks());
                return list;
            default:
                return null;
        }
    }

    @Override
    public void deleteAllItems() {
        taskMap.clear();
        epicMap.clear();
        subtaskMap.clear();
        historyManager.clear();
        prioritySet.clear();
        id = 1;
    }

    @Override
    public void deleteTaskById(int id) {
        idCheck(id);
        if (taskMap.containsKey(id)) {
            historyManager.remove(id);
            prioritySet.remove(taskMap.get(id));
            taskMap.remove(id);
        } else if (epicMap.containsKey(id)) {
            List<Integer> subTasksIdList = epicMap.get(id).getSubTasksIdList();
            if (!subTasksIdList.isEmpty()) {
                for (int subtaskId : subTasksIdList) {
                    historyManager.remove(subtaskId);
                    prioritySet.remove(subtaskMap.get(subtaskId));
                    subtaskMap.remove(subtaskId);
                }
            }
            historyManager.remove(id);
            epicMap.remove(id);
        } else if (subtaskMap.containsKey(id)) {
            int epicId = subtaskMap.get(id).getEpicId();
            historyManager.remove(id);
            prioritySet.remove(subtaskMap.get(id));
            epicMap.get(epicId).removeSubtaskIdFromEpicList(id);
            subtaskMap.remove(id);
            updateEpicFields(epicId);
        }
    }

    @Override
    public void deleteTasksByType(Type type) {
        switch (type) {
            case TASK:
                for (Task task : taskMap.values()) {
                    prioritySet.remove(task);
                    historyManager.remove(task.getId());
                }
                taskMap.clear();
                break;
            case EPIC:
                for (Epic epic : epicMap.values()) {
                    historyManager.remove(epic.getId());
                }
                epicMap.clear();
                for (Subtask subtask : subtaskMap.values()) {
                    historyManager.remove(subtask.getId());
                    prioritySet.remove(subtask);
                }
                subtaskMap.clear();
                break;
            case SUBTASK:
                for (Subtask subtask : subtaskMap.values()) {
                    historyManager.remove(subtask.getId());
                    prioritySet.remove(subtask);
                }
                subtaskMap.clear();
                for (Epic epic : epicMap.values()) {
                    epic.getSubTasksIdList().clear();
                    updateEpicFields(epic.getId());
                }
                break;
        }
    }

    @Override
    public Task getTaskById(int id) {
        idCheck(id);
        if (taskMap.containsKey(id)) {
            return getTask(id);
        } else if (epicMap.containsKey(id)) {
            return getEpic(id);
        } else if (subtaskMap.containsKey(id)) {
            return getSubtask(id);
        }
        return null;
    }

    @Override
    public List<Subtask> getEpicsSubtasksById(int epicId) {
        idCheck(epicId);
        if (epicMap.isEmpty()) {
            throw new NoSuchTaskException("Unable to get subtasks! There's no epics!");
        } else if (!epicMap.containsKey(epicId)) {
            throw new NoSuchTaskException("Unable to get subtasks! There's no epics with such epicId");
        } else if (epicMap.get(epicId).getSubTasksIdList().isEmpty()) {
            throw new NoSuchTaskException("Epic has no subtasks!");
        }
        List<Integer> subtasksIdList = epicMap.get(epicId).getSubTasksIdList();
        List<Subtask> subtaskList = new ArrayList<>();
        for (int subtaskId : subtasksIdList) {
            subtaskList.add(subtaskMap.get(subtaskId));
        }
        return subtaskList;
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritySet);

    }

    public void idCheck(int id) {
        if (id == 0) {
            throw new NoSuchTaskException("Task has no id!");
        } else if (getAllItems().stream().noneMatch(task -> task.getId() == id)) {
            throw new NoSuchTaskException("There's no task with such id!");
        }
    }

    private int createId() {
        int maxId = 0;
        List<Task> allTasks = getAllItems();
        if (!allTasks.isEmpty()) {
            for (Task task : allTasks) {
                if (task.getId() > maxId) {
                    maxId = task.getId();
                }
            }
        }
        id = maxId;
        return ++id;
    }

    private void updateEpicFields(int epicId) {
        updateEpicStatus(epicId);
        updateEpicTemporal(epicId);
    }

    private void updateEpicStatus(int epicId) {
        Epic existingEpic = epicMap.get(epicId);
        List<Integer> subtasksIdList = existingEpic.getSubTasksIdList();
        int newSubtasksCount = 0;
        int doneSubtasksCount = 0;
        if (!subtasksIdList.isEmpty() && !subtaskMap.isEmpty()) {
            for (int subtaskId : subtasksIdList) {
                if (subtaskMap.get(subtaskId).getStatus() == Status.NEW) {
                    newSubtasksCount++;
                } else if (subtaskMap.get(subtaskId).getStatus() == Status.DONE) {
                    doneSubtasksCount++;
                } else {
                    existingEpic.setStatus(Status.IN_PROGRESS);
                    return;
                }
            }
            if (newSubtasksCount == subtasksIdList.size()) {
                existingEpic.setStatus(Status.NEW);
            } else if (doneSubtasksCount == subtasksIdList.size()) {
                existingEpic.setStatus(Status.DONE);
            } else {
                existingEpic.setStatus(Status.IN_PROGRESS);
            }
        } else {
            existingEpic.setStatus(Status.NEW);
            existingEpic.setStartTime(null);
            existingEpic.setDuration(0);
            existingEpic.setEndTime(null);
        }
    }

    private void updateEpicTemporal(int epicId) {
        Epic epic = epicMap.get(epicId);
        if (epic == null) {
            return;
        }
        if (!epic.getSubTasksIdList().isEmpty()) {
            List<Subtask> subtasksIdList = getEpicsSubtasksById(epicId);
            long epicDuration = 0;
            TreeSet<LocalDateTime> timeSet = new TreeSet<>((startTime1, startTime2) ->
                    startTime1.isBefore(startTime2) ? -1 : startTime1.isAfter(startTime2) ? 1 : 0);
            for (Subtask subtask : subtasksIdList) {
                if (subtask.getStartTime() != null) {
                    timeSet.add(subtask.getStartTime());
                }
                epicDuration += subtask.getDuration();
                if (subtask.getEndTime() != null) {
                    timeSet.add(subtask.getEndTime());
                }
            }
            if (!(timeSet.isEmpty() && epicDuration == 0)) {
                epic.setStartTime(timeSet.first());
                epic.setDuration(epicDuration);
                epic.setEndTime(timeSet.last());
            }
        } else {
            epic.setStartTime(null);
            epic.setDuration(0);
            epic.setEndTime(null);
        }
    }

    private void intersectingCheck(Task newTask) {
        if (newTask.getStartTime() == null || prioritySet.isEmpty()) {
            return;
        }
        if (newTask.getEndTime().isBefore(newTask.getStartTime())) {
            throw new AddingAndUpdatingException(
                    "The adding task end time is before start time or start time is after end time!");
        }
        for (Task task : prioritySet) {
            if (task.getStartTime() == null) {
                return;
            }
            if (task.getId() == newTask.getId()) {
                continue;
            }
            if (task.getStartTime().isBefore(newTask.getEndTime())
                    && task.getEndTime().isAfter(newTask.getStartTime())) {
                throw new AddingAndUpdatingException(
                        "The adding task intersects in execution time with an existing task!");
            }
        }
    }

}

