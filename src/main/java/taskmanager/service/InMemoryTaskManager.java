package taskmanager.service;

import taskmanager.exceptions.AddingAndUpdatingException;
import taskmanager.exceptions.NoSuchTaskException;
import taskmanager.model.*;

import java.time.ZonedDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private int id = 1;

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
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public Task getTask(int taskId) throws NoSuchTaskException {
        idCheck(taskId);
        Task task = taskMap.get(taskId);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpic(int epicId) throws NoSuchTaskException {
        idCheck(epicId);
        Epic epic = epicMap.get(epicId);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Subtask getSubtask(int subtaskId) throws NoSuchTaskException {
        idCheck(subtaskId);
        Subtask subtask = subtaskMap.get(subtaskId);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public void addNewTask(Task task) throws AddingAndUpdatingException, NoSuchTaskException {
        IntersectingCheck(task);
        task.setId(createId());
        prioritySet.add(task);
        taskMap.put(task.getId(), task);
    }

    @Override
    public void addNewEpic(Epic epic) throws AddingAndUpdatingException, NoSuchTaskException {
        epic.setId(createId());
        epic.setSubTasksIdList(new ArrayList<>());
        epic.setStartTime(null);
        epic.setDuration(0);
        epic.setEndTime(null);
        prioritySet.add(epic);
        epicMap.put(epic.getId(), epic);
        updateEpicFields(epic.getId());
    }

    @Override
    public void addNewSubtask(Subtask subtask) throws AddingAndUpdatingException, NoSuchTaskException {
        IntersectingCheck(subtask);
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
    public void updateTask(Task newTask) throws AddingAndUpdatingException, NoSuchTaskException {
        idCheck(newTask.getId());
        Task existingTask = taskMap.get(newTask.getId());
        deleteTaskById(existingTask.getId());
        try {
            IntersectingCheck(existingTask);
        } catch (AddingAndUpdatingException e) {
            prioritySet.add(existingTask);
            taskMap.put(existingTask.getId(), existingTask);
            throw new AddingAndUpdatingException("Updated task intersects in execution time with an existing task!");
        }
        existingTask.setTitle(newTask.getTitle());
        existingTask.setDescription(newTask.getDescription());
        existingTask.setStatus(newTask.getStatus());
        existingTask.setStartTime(newTask.getStartTime());
        existingTask.setDuration(newTask.getDuration());
        prioritySet.add(existingTask);
        taskMap.put(existingTask.getId(), existingTask);
    }

    @Override
    public void updateEpic(Epic newEpic) throws AddingAndUpdatingException, NoSuchTaskException {
        idCheck(newEpic.getId());
        Epic existingEpic = epicMap.get(newEpic.getId());
        deleteTaskById(existingEpic.getId());
        existingEpic.setSubTasksIdList(new ArrayList<>());
        existingEpic.setStartTime(null);
        existingEpic.setDuration(0);
        existingEpic.setEndTime(null);
        existingEpic.setTitle(newEpic.getTitle());
        existingEpic.setDescription(newEpic.getDescription());
        prioritySet.add(existingEpic);
        epicMap.put(existingEpic.getId(), existingEpic);
        updateEpicFields(existingEpic.getId());
    }

    @Override
    public void updateSubtask(Subtask newSubtask) throws AddingAndUpdatingException, NoSuchTaskException {
        idCheck(newSubtask.getId());
        Subtask existingSubtask = subtaskMap.get(newSubtask.getId());
        deleteTaskById(newSubtask.getId());
        updateEpicFields(existingSubtask.getEpicId());
        try {
            IntersectingCheck(newSubtask);
        } catch (AddingAndUpdatingException e) {
            addNewSubtask(existingSubtask);
            throw new AddingAndUpdatingException("Updated subtask intersects in execution time with an existing task!");
        }
        existingSubtask.setTitle(newSubtask.getTitle());
        existingSubtask.setDescription(newSubtask.getDescription());
        existingSubtask.setStatus(newSubtask.getStatus());
        existingSubtask.setStartTime(newSubtask.getStartTime());
        existingSubtask.setDuration(newSubtask.getDuration());
        prioritySet.add(existingSubtask);
        subtaskMap.put(existingSubtask.getId(), existingSubtask);
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
    }

    @Override
    public void deleteTaskById(int id) throws NoSuchTaskException {
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
            prioritySet.remove(epicMap.get(id));
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
    public void deleteTasksByType(Type type) throws NoSuchTaskException {
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
                    prioritySet.remove(epic);
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
    public Task getTaskById(int id) throws NoSuchTaskException {
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
    public List<Subtask> getEpicsSubtasksById(int epicId) throws NoSuchTaskException {
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

    private void updateEpicFields(int epicId) throws NoSuchTaskException {
        updateEpicStatus(epicId);
        UpdateEpicTemporal(epicId);
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
                existingEpic.setStatus(Status.NEW);
            }
        } else {
            epicMap.get(epicId).setStatus(Status.NEW);
            existingEpic.setStartTime(null);
            existingEpic.setDuration(0);
            existingEpic.setEndTime(null);
        }
    }

    private void UpdateEpicTemporal(int epicId) throws NoSuchTaskException {
        if (!epicMap.get(epicId).getSubTasksIdList().isEmpty()) {
            Epic epic = epicMap.get(epicId);
            List<Subtask> subtasksIdList = getEpicsSubtasksById(epicId);
            long epicDuration = 0;
            TreeSet<ZonedDateTime> timeSet = new TreeSet<>((startTime1, startTime2) ->
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
                prioritySet.remove(epic);
                epic.setStartTime(timeSet.first());
                epic.setDuration(epicDuration);
                epic.setEndTime(timeSet.last());
                prioritySet.add(epic);
            }
        } else {
            Epic epic = epicMap.get(epicId);
            epic.setStartTime(null);
            epic.setDuration(0);
            epic.setEndTime(null);
        }
    }

    private void IntersectingCheck(Task newTask) throws AddingAndUpdatingException {
        if (newTask.getStartTime() == null || newTask.getDuration() == 0 || prioritySet.isEmpty()) {
            return;
        }
        for (Task task : prioritySet) {
            if (!(task.getStartTime() == null || task.getDuration() == 0)) {
                if (task.getStartTime().isBefore(newTask.getEndTime())
                        && task.getEndTime().isAfter(newTask.getStartTime())) {
                    throw new AddingAndUpdatingException(
                            "The adding task intersects in execution time with an existing task!");
                }
            }
            return;
        }
    }

    private void idCheck(int id) throws NoSuchTaskException {
        if (id == 0) {
            throw new NoSuchTaskException("Task has no id!");
        } else if (getAllItems().stream().noneMatch(task -> task.getId() == id)) {
            throw new NoSuchTaskException("There's no task with such id!");
        }
    }
}
