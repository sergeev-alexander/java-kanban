package taskmanager.service;

import taskmanager.exceptions.ManagerSaveException;
import taskmanager.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTasksManager extends InMemoryTaskManager {

    private File backupFile;
    private static final String CSV_HEADER = "id,type,name,status,description,startTime,duration,endTime,epic,subtasks\n";

    public FileBackedTasksManager() {
        super(Managers.getDefaultHistoryManager());
    }
    public FileBackedTasksManager(HistoryManager historyManager, String backupFileName) {
        super(historyManager);
        File backupFile = new File("BackupDirectory", backupFileName);
        if (backupFile.exists()) {
            this.backupFile = backupFile;
            backup(load(backupFile));
        } else {
            this.backupFile = createNewBackupFile(backupFile);
        }
    }

    public static FileBackedTasksManager loadBackup(String backupFileName) throws ManagerSaveException {
        return new FileBackedTasksManager(Managers.getDefaultHistoryManager(), backupFileName);
    }

    @Override
    public int getIdField() {
        return super.getIdField();
    }

    @Override
    public Task getTask(int taskId) {
        Task task = super.getTask(taskId);
        save();
        return task;
    }

    @Override
    public Epic getEpic(int epicId) {
        Epic epic = super.getEpic(epicId);
        save();
        return epic;
    }

    @Override
    public Subtask getSubtask(int subtaskId) {
        Subtask subtask = super.getSubtask(subtaskId);
        save();
        return subtask;
    }

    @Override
    public void updateTask(Task newTask) {
        super.updateTask(newTask);
        save();
    }

    @Override
    public void addNewTask(Task task) {
        super.addNewTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic newEpic) {
        super.updateEpic(newEpic);
        save();
    }

    @Override
    public void addNewEpic(Epic epic) {
        super.addNewEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask newSubtask) {
        super.updateSubtask(newSubtask);
        save();
    }

    @Override
    public void addNewSubtask(Subtask subtask) {
        super.addNewSubtask(subtask);
        save();
    }

    @Override
    public void deleteAllItems() {
        super.deleteAllItems();
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteTasksByType(Type type) {
        super.deleteTasksByType(type);
        save();
    }

    @Override
    public void idCheck(int id) {
        super.idCheck(id);
    }

    private File createNewBackupFile(File newBackupFile) throws ManagerSaveException {
        try {
            newBackupFile.createNewFile();
            return newBackupFile;
        } catch (IOException e) {
            throw new ManagerSaveException("Unable to create new file", e.getCause());
        }
    }

    private List<String> load(File file) throws ManagerSaveException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            List<String> lines = new ArrayList<>();
            while (reader.ready()) {
                lines.add(reader.readLine());
            }
            return lines;
        } catch (IOException e) {
            throw new ManagerSaveException("Unable to load from file!", e.getCause());
        }
    }

    protected void backup(List<String> lines) {
        if (lines.isEmpty()) {
            return;
        }
        for (int i = 1; i < lines.size() - 1; i++) {
            String[] line = lines.get(i).split(",");
            if (line.length > 1) {
                switch (Type.valueOf(line[1])) {
                    case TASK:
                        Task task = new Task();
                        task.setId(Integer.parseInt(line[0]));
                        task.setTitle(line[2]);
                        if (!line[3].equals("null")) {
                            task.setStatus(Status.valueOf(line[3]));
                        }
                        task.setDescription(line[4]);
                        if (!line[5].equals("null")) {
                            task.setStartTime(LocalDateTime.parse(line[5]));
                        }
                        task.setDuration(Long.parseLong(line[6]));
                        taskMap.put(task.getId(), task);
                        prioritySet.add(task);
                        break;
                    case EPIC:
                        Epic epic = new Epic();
                        epic.setId(Integer.parseInt(line[0]));
                        epic.setTitle(line[2]);
                        if (!line[3].equals("null")) {
                            epic.setStatus(Status.valueOf(line[3]));
                        }
                        epic.setDescription(line[4]);
                        if (!line[5].equals("null")) {
                            epic.setStartTime(LocalDateTime.parse(line[5]));
                        }
                        epic.setDuration(Long.parseLong(line[6]));
                        if (!line[7].equals("null")) {
                            epic.setEndTime(LocalDateTime.parse(line[7]));
                        }
                        List<Integer> subtaskList = new ArrayList<>();
                        for (int j = 9; j < line.length; j++) {
                            if (!line[j].isEmpty()) {
                                subtaskList.add(Integer.parseInt(line[j]));
                            }
                        }
                        epic.setSubTasksIdList(subtaskList);
                        epicMap.put(epic.getId(), epic);
                        break;
                    case SUBTASK:
                        Subtask subtask = new Subtask();
                        subtask.setId(Integer.parseInt(line[0]));
                        subtask.setTitle(line[2]);
                        if (!line[3].equals("null")) {
                            subtask.setStatus(Status.valueOf(line[3]));
                        }
                        subtask.setDescription(line[4]);
                        if (!line[5].equals("null")) {
                            subtask.setStartTime(LocalDateTime.parse(line[5]));
                        }
                        subtask.setDuration(Long.parseLong(line[6]));
                        subtask.setEpicId(Integer.parseInt(line[8]));
                        subtaskMap.put(subtask.getId(), subtask);
                        prioritySet.add(subtask);
                        break;
                }
            }
        }
        if (!lines.get(lines.size() - 1).isEmpty() || !lines.get(lines.size() - 1).isBlank()) {
            String[] historyLine = lines.get(lines.size() - 1).split(",");
            for (int k = historyLine.length - 1; k >= 0; k--) {
                historyManager.add(getTaskById(Integer.parseInt(historyLine[k])));
            }
        }
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
    }

    protected void save() {
        try (FileWriter writer = new FileWriter(backupFile, StandardCharsets.UTF_8)) {
            writer.write(createCSV());
        } catch (IOException e) {
            throw new ManagerSaveException("Unable to save file!", e.getCause());
        }
    }

    private String createCSV() {
        List<Task> taskList = getAllItems();
        StringBuilder CSV = new StringBuilder();
        if (!taskList.isEmpty()) {
            CSV.append(CSV_HEADER);
            for (Task task : taskList) {
                CSV.append(task.toString());
            }
        }
        CSV.append("\n");
        List<Task> historyList = getHistory();
        if (!historyList.isEmpty()) {
            for (Task task : historyList) {
                CSV.append(task.getId()).append(",");
            }
            CSV.deleteCharAt(CSV.length() - 1);
        }
        return String.valueOf(CSV);
    }

}

