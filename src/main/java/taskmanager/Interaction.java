package taskmanager;

import taskmanager.exceptions.AddingAndUpdatingException;
import taskmanager.exceptions.NoSuchTaskException;
import taskmanager.model.*;
import taskmanager.service.FileBackedTasksManager;
import taskmanager.service.TaskManager;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Interaction {

    public static DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    public static ZoneId ZONE_ID = ZoneId.of("Europe/Moscow");

    TaskManager taskManager = FileBackedTasksManager.loadFromFile(new File("Backup_file_1.csv"));
    Scanner sc = new Scanner(System.in);

    public Interaction() throws NoSuchTaskException, AddingAndUpdatingException {
    }

    public void interaction() {
        while (true) {
            printMenu();
            String input = sc.nextLine();
            int command = inputToInt(input);
            switch (command) {
                case 1:
                    createNewTask(userSelectTaskType());
                    break;
                case 2:
                    System.out.println(taskManager.getAllItems());
                    break;
                case 3:
                    if (taskManager.getAllItems().isEmpty()) {
                        System.out.println("There's no tasks to show!");
                    } else {
                        System.out.println(taskManager.getTasksByType(userTypeChoice(userSelectTaskType())));
                    }
                    break;
                case 4:
                    taskManager.deleteAllItems();
                    break;
                case 5:
                    try {
                        taskManager.deleteTasksByType(userTypeChoice(userSelectTaskType()));
                    } catch (NoSuchTaskException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case 6:
                    if (taskManager.getAllItems().isEmpty()) {
                        System.out.println("There's no tasks to show!");
                    } else {
                        try {
                            System.out.println(taskManager.getTaskById(userSelectId()));
                        } catch (NoSuchTaskException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    break;
                case 7:
                    updateExistingTask();
                    break;
                case 8:
                    try {
                        taskManager.deleteTaskById(userSelectId());
                    } catch (NoSuchTaskException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case 9:
                    if (taskManager.getAllEpics().isEmpty()) {
                        System.out.println("Unable to show subtasks! There's no epics!");
                    } else {
                        try {
                            System.out.println(taskManager.getEpicsSubtasksById(userSelectEpicId()));
                        } catch (NoSuchTaskException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    break;
                case 10:
                    System.out.println(taskManager.getHistory());
                    break;
                case 11:
                    System.out.println(taskManager.getPrioritizedTasks());
                    break;
                case 0:
                    System.out.println("Good bye!");
                    return;
                default:
                    System.out.println("[" + input + "] - Wrong command!" +
                            "\nTry again!");
                    break;
            }
        }
    }

    private void printMenu() {
        System.out.println("\nWhat are you going to do?" +
                "\n(1)  - Add new task" +
                "\n(2)  - Show all tasks" +
                "\n(3)  - Show tasks by type" +
                "\n(4)  - Delete all tasks" +
                "\n(5)  - Delete tasks by type" +
                "\n(6)  - Show task by id" +
                "\n(7)  - Update existing task" +
                "\n(8)  - Delete task by id" +
                "\n(9)  - Show subtasks of an epic" +
                "\n(10) - Show history of views" +
                "\n(11) - Show PrioritizedTasks list" +
                "\n(0) - Exit app");
    }

    private int userSelectTaskType() {
        while (true) {
            System.out.println("Select task type!" +
                    "\n(1) - Task" +
                    "\n(2) - Epic" +
                    "\n(3) - Subtask");
            String input = sc.nextLine();
            int command = inputToInt(input);
            if (command < 1 || command > 3) {
                System.out.println("Wrong task type!");
            } else {
                return command;
            }
        }
    }

    private int userSelectId() {
        while (true) {
            System.out.println("Insert id!");
            String input = sc.nextLine();
            int id = inputToInt(input);
            if (id > 0) {
                List<Task> allTasksList = taskManager.getAllItems();
                for (Task task : allTasksList) {
                    if (task.getId() == id) {
                        return id;
                    }
                }
                System.out.println("There's no task with [" + id + "] id" +
                        "\nTry again!");
            } else {
                System.out.println("[" + input + "] - Wrong id!" +
                        "\nTry again!");
            }
        }
    }

    private int userSelectEpicId() {
        while (true) {
            System.out.println("Insert epic id!");
            String input = sc.nextLine();
            int id = inputToInt(input);
            if (id > 0) {
                for (Epic epic : taskManager.getAllEpics()) {
                    if (epic.getId() == id) {
                        return id;
                    }
                }
                System.out.println("There's no task with [" + id + "] id" +
                        "\nTry again!");
            } else {
                System.out.println("[" + input + "] - Wrong id!" +
                        "\nTry again!");
            }
        }
    }

    private int inputToInt(String userInput) {
        String input = userInput.trim();
        if (input.equals("")) {
            return -1;
        }
        for (int i = 0; i < input.length(); i++) {
            if (!Character.isDigit(input.charAt(i))) {
                return -1;
            }
        }
        return Integer.parseInt(input);
    }

    private void createNewTask(int command) {
        switch (command) {
            case 1:
                addNewTask();
                break;
            case 2:
                addNewEpic();
                break;
            case 3:
                if (taskManager.getTasksByType(Type.EPIC).isEmpty()) {
                    System.out.println("Unable to create subtask! There is no epics.");
                    return;
                }
                addNewSubtask();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + command);
        }
    }

    private void addNewTask() {
        Task task = new Task();
        System.out.println("Insert task title!");
        String title = sc.nextLine();
        task.setTitle(title);
        System.out.println("Insert task description!");
        String description = sc.nextLine();
        task.setDescription(description);
        System.out.println("Insert task startDateTime in format \"dd.MM.yyyy HH:mm\"");
        String startDateTime = sc.nextLine();
        try {
            task.setStartTime(ZonedDateTime.of(LocalDateTime.parse(startDateTime, DT_FORMATTER), ZONE_ID));
        } catch (DateTimeParseException e) {
            System.out.println("Wrong DateTime format! Try again!");
        }
        System.out.println("Insert task duration (min)");
        String durationString = sc.nextLine();
        task.setDuration(inputToInt(durationString));
        task.setStatus(Status.NEW);
        try {
            taskManager.addNewTask(task);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
    }

    private void addNewEpic() {
        Epic epic = new Epic();
        System.out.println("Insert epic title!");
        String title = sc.nextLine();
        epic.setTitle(title);
        System.out.println("Insert epic description!");
        String description = sc.nextLine();
        epic.setDescription(description);
        epic.setStatus(Status.NEW);
        try {
            taskManager.addNewEpic(epic);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
    }

    private void addNewSubtask() {
        int epicId = userSelectEpicId();
        Subtask subtask = new Subtask();
        subtask.setEpicId(epicId);
        System.out.println("Insert subtask title!");
        String title = sc.nextLine();
        subtask.setTitle(title);
        System.out.println("Insert subtask description!");
        String description = sc.nextLine();
        subtask.setDescription(description);
        System.out.println("Insert subtask startDateTime in format \"dd.MM.yyyy HH:mm\"");
        String startDateTime = sc.nextLine();
        try {
            subtask.setStartTime(ZonedDateTime.of(LocalDateTime.parse(startDateTime, DT_FORMATTER), ZONE_ID));
        } catch (DateTimeParseException e) {
            System.out.println("Wrong DateTime format! Try again!");
        }
        System.out.println("Insert subtask duration (min)");
        String durationString = sc.nextLine();
        subtask.setDuration(inputToInt(durationString));
        subtask.setStatus(Status.NEW);
        try {
            taskManager.addNewSubtask(subtask);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
    }

    private Type userTypeChoice(int command) {
        switch (command) {
            case 1:
                return Type.TASK;
            case 2:
                return Type.EPIC;
            case 3:
                return Type.SUBTASK;
            default:
                throw new IllegalStateException("Unexpected value: " + command);
        }
    }

    private void updateExistingTask() {
        while (true) {
            if (taskManager.getAllItems().isEmpty()) {
                System.out.println("There's no tasks to change!");
                return;
            }
            System.out.println("Input task id you want to change!");
            String userInput = sc.nextLine();
            int id = inputToInt(userInput);
            try {
                if (taskManager.getTasksByType(Type.TASK).contains(taskManager.getTaskById(id))) {
                    taskManager.deleteTaskById(id);
                    updateTask(id);
                    return;
                } else if (taskManager.getTasksByType(Type.EPIC).contains(taskManager.getTaskById(id))) {
                    taskManager.deleteTaskById(id);
                    updateEpic(id);
                    return;
                } else if (taskManager.getTasksByType(Type.SUBTASK).contains(taskManager.getTaskById(id))) {
                    int epicId = taskManager.getSubtask(id).getEpicId();
                    taskManager.deleteTaskById(id);
                    updateSubtask(id, epicId);
                    return;
                } else {
                    System.out.println("There's no task with [" + userInput + "] id!" +
                            "\nTry again!");
                }
            } catch (NoSuchTaskException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private Status updateStatus() {
        while (true) {
            System.out.println("Insert status!" +
                    "\n(1) - NEW" +
                    "\n(2) - IN_PROGRESS" +
                    "\n(3) - DONE");
            String input = sc.nextLine();
            int command = inputToInt(input);
            switch (command) {
                case 1:
                    return Status.NEW;
                case 2:
                    return Status.IN_PROGRESS;
                case 3:
                    return Status.DONE;
                default:
                    System.out.println("[" + input + "] - Wrong command!" +
                            "\nTry again!");
                    break;
            }
        }
    }

    private void updateTask(int id) {
        Task task = new Task();
        task.setId(id);
        System.out.println("Insert new task title!");
        String title = sc.nextLine();
        task.setTitle(title);
        System.out.println("Insert new task description!");
        String description = sc.nextLine();
        task.setDescription(description);
        task.setStatus(updateStatus());
        boolean flag = false;
        while (!flag) {
            try {
                System.out.println("Insert new task startDateTime in format \"dd.MM.yyyy HH:mm\"");
                String startDateTime = sc.nextLine();
                task.setStartTime(ZonedDateTime.of(LocalDateTime.parse(startDateTime, DT_FORMATTER), ZONE_ID));
                flag = true;
            } catch (DateTimeParseException e) {
                System.out.println("Wrong DateTime format! Try again!");
            }
        }
        long duration = -1;
        while (duration == -1) {
            System.out.println("Insert new task duration (min)");
            String durationString = sc.nextLine();
            duration = (inputToInt(durationString));
        }
        task.setDuration(duration);
        try {
            taskManager.addNewTask(task);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
    }


    private void updateEpic(int id) {
        Epic epic = new Epic();
        epic.setId(id);
        System.out.println("Insert new epic title!");
        String title = sc.nextLine();
        epic.setTitle(title);
        System.out.println("Insert new epic description!");
        String description = sc.nextLine();
        epic.setDescription(description);
        try {
            taskManager.updateEpic(epic);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
    }

    private void updateSubtask(int id, int epicId) {
        Subtask subtask = new Subtask();
        subtask.setId(id);
        subtask.setEpicId(epicId);
        System.out.println("Insert new subtask title!");
        String title = sc.nextLine();
        subtask.setTitle(title);
        System.out.println("Insert new subtask description!");
        String description = sc.nextLine();
        subtask.setDescription(description);
        boolean flag = false;
        while (!flag) {
            try {
                System.out.println("Insert new subtask startDateTime in format \"dd.MM.yyyy HH:mm\"");
                String startDateTime = sc.nextLine();
                subtask.setStartTime(ZonedDateTime.of(LocalDateTime.parse(startDateTime, DT_FORMATTER), ZONE_ID));
                flag = true;
            } catch (DateTimeParseException e) {
                System.out.println("Wrong DateTime format! Try again!");
            }
        }
        long duration = -1;
        while (duration == -1) {
            System.out.println("Insert new subtask duration (min)");
            String durationString = sc.nextLine();
            duration = (inputToInt(durationString));
        }
        subtask.setDuration(duration);
        subtask.setStatus(updateStatus());
        try {
            taskManager.updateSubtask(subtask);
        } catch (AddingAndUpdatingException | NoSuchTaskException e) {
            System.out.println(e.getMessage());
        }
    }

}