import java.util.Scanner;

public class Interaction {

    Scanner sc = new Scanner(System.in);
    Manager manager = new Manager();

    public void interaction() {
        while (true) {
            printMenu();
            String input = sc.nextLine();
            int command = inputToInt(input);
            switch (command) {
                case 1:
                    manager.updateTask(createNewTask(userSelectTaskType()));
                    break;
                case 2:
                    System.out.println(manager.getAllTasks());
                    break;
                case 3:
                    System.out.println(manager.getTasksByType(userTypeChoice(userSelectTaskType())));
                    break;
                case 4:
                    manager.deleteAllTasks();
                    break;
                case 5:
                    manager.deleteTasksByType(userTypeChoice(userSelectTaskType()));
                    break;
                case 6:
                    System.out.println(manager.getTaskById(userSelectId()));
                    break;
                case 7:
                    manager.updateTask(updateExistingTask());
                    break;
                case 8:
                    manager.deleteTaskById(userSelectId());
                    break;
                case 9:
                    System.out.println(manager.getEpicsSubtasksById(userSelectEpicId()));
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
                "\n(1) - Add new task" +
                "\n(2) - Show all tasks" +
                "\n(3) - Show tasks by type" +
                "\n(4) - Delete all tasks" +
                "\n(5) - Delete tasks by type" +
                "\n(6) - Show task by id" +
                "\n(7) - Update existing task" +
                "\n(8) - Delete task by id" +
                "\n(9) - Show subtasks of an epic" +
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
                for (Task task : manager.getTaskMap().values()) {
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
                for (Task task : manager.getTaskMap().values()) {
                    if (task.getId() == id && task.getType().equals(Type.EPIC)) {
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

    private Task createNewTask(int command) {
        switch (command) {
            case 1:
                return addNewTask();
            case 2:
                return addNewEpic();
            case 3:
                return addNewSubtask();
            default:
                throw new IllegalStateException("Unexpected value: " + command);
        }
    }

    private Task addNewTask() {
        Task task = new Task();
        task.setId(manager.createId());
        System.out.println("Insert task title!");
        String title = sc.nextLine();
        task.setTitle(title);
        System.out.println("Insert task description!");
        String description = sc.nextLine();
        task.setDescription(description);
        task.setStatus(Status.NEW);
        return task;
    }

    private Task addNewEpic() {
        Epic epic = new Epic();
        epic.setId(manager.createId());
        System.out.println("Insert epic title!");
        String title = sc.nextLine();
        epic.setTitle(title);
        System.out.println("Insert epic description!");
        String description = sc.nextLine();
        epic.setDescription(description);
        epic.setStatus(Status.NEW);
        return epic;
    }

    private Task addNewSubtask() {
        int epicId = userSelectEpicId();
        Subtask subtask = new Subtask();
        int subtaskId = manager.createId();
        subtask.setId(subtaskId);
        subtask.setEpicId(epicId);
        System.out.println("Insert subtask title!");
        String title = sc.nextLine();
        subtask.setTitle(title);
        System.out.println("Insert subtask description!");
        String description = sc.nextLine();
        subtask.setDescription(description);
        subtask.setStatus(Status.NEW);
        ((Epic) manager.getTaskMap().get(epicId)).setSubtasksIdToEpicList(subtaskId);
        return subtask;

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

    private Task updateExistingTask() {
        while (true) {
            System.out.println("Input task id you want to change!");
            String userInput = sc.nextLine();
            int id = inputToInt(userInput);
            if (manager.getTaskMap().containsKey(id)) {
                if (manager.getTaskMap().get(id).getType().equals(Type.TASK)) {
                    return updateTask(id);
                } else if (manager.getTaskMap().get(id).getType().equals(Type.EPIC)) {
                    return updateEpic(id);
                } else {
                    return updateSubtask(id);
                }
            } else {
                System.out.println("There's no task with [" + userInput + "] id!" +
                        "\nTry again!");
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

    private Task updateTask(int id) {
        Task task = new Task();
        task.setId(id);
        System.out.println("Insert new task title!");
        String title = sc.nextLine();
        task.setTitle(title);
        System.out.println("Insert new task description!");
        String description = sc.nextLine();
        task.setDescription(description);
        task.setStatus(updateStatus());
        return task;
    }

    private Epic updateEpic(int id) {
        Epic epic = new Epic();
        epic.setId(id);
        System.out.println("Insert new epic title!");
        String title = sc.nextLine();
        epic.setTitle(title);
        System.out.println("Insert new epic description!");
        String description = sc.nextLine();
        epic.setDescription(description);
        epic.setStatus(updateStatus());
        epic.setSubTasksIdList(((Epic) manager.getTaskMap().get(id)).getSubTasksIdList());
        return epic;
    }

    private Subtask updateSubtask(int id) {
        Subtask subtask = new Subtask();
        subtask.setId(id);
        subtask.setEpicId(((Subtask) manager.getTaskMap().get(id)).getEpicId());
        System.out.println("Insert new subtask title!");
        String title = sc.nextLine();
        subtask.setTitle(title);
        System.out.println("Insert new subtask description!");
        String description = sc.nextLine();
        subtask.setDescription(description);
        subtask.setStatus(updateStatus());
        return subtask;
    }
}