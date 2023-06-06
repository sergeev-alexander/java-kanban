package com.yandex.taskmanager;

import com.yandex.taskmanager.model.*;
import com.yandex.taskmanager.service.Manager;

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
                    createNewTask(userSelectTaskType());
                    break;
                case 2:
                    System.out.println(manager.getAllItems());
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
                    updateExistingTask();
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
                for (Task task : manager.getAllTasks()) {
                    if (task.getId() == id) {
                        return id;
                    }
                }
                for (Epic epic : manager.getAllEpics()) {
                    if (epic.getId() == id) {
                        return id;
                    }
                }
                for (Subtask subtask : manager.getAllSubtasks()) {
                    if (subtask.getId() == id) {
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
                for (Epic epic : manager.getAllEpics()) {
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
                addNewSubtask();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + command);
        }
    }

    private void addNewTask() {
        Task task = new Task();
        task.setId(manager.getId());
        System.out.println("Insert task title!");
        String title = sc.nextLine();
        task.setTitle(title);
        System.out.println("Insert task description!");
        String description = sc.nextLine();
        task.setDescription(description);
        task.setStatus(Status.NEW);
        manager.addNewTask(task);
    }

    private void addNewEpic() {
        Epic epic = new Epic();
        epic.setId(manager.getId());
        System.out.println("Insert epic title!");
        String title = sc.nextLine();
        epic.setTitle(title);
        System.out.println("Insert epic description!");
        String description = sc.nextLine();
        epic.setDescription(description);
        epic.setStatus(Status.NEW);
        manager.addNewEpic(epic);
    }

    private void addNewSubtask() {
        int epicId = userSelectEpicId();
        Subtask subtask = new Subtask();
        int subtaskId = manager.getId();
        subtask.setId(subtaskId);
        subtask.setEpicId(epicId);
        System.out.println("Insert subtask title!");
        String title = sc.nextLine();
        subtask.setTitle(title);
        System.out.println("Insert subtask description!");
        String description = sc.nextLine();
        subtask.setDescription(description);
        subtask.setStatus(Status.NEW);
        manager.addNewSubtask(subtask);
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
            System.out.println("Input task id you want to change!");
            String userInput = sc.nextLine();
            int id = inputToInt(userInput);
            if (manager.getTasksByType(Type.TASK).contains(manager.getTaskById(id))) {
                updateTask(manager.getTaskById(id));
                return;
            } else if (manager.getTasksByType(Type.EPIC).contains(manager.getTaskById(id))) {
                updateEpic((Epic) manager.getTaskById(id));
                return;
            } else if (manager.getTasksByType(Type.SUBTASK).contains(manager.getTaskById(id))) {
                updateSubtask((Subtask) manager.getTaskById(id));
                return;
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

    private void updateTask(Task task) {
        System.out.println("Insert new task title!");
        String title = sc.nextLine();
        task.setTitle(title);
        System.out.println("Insert new task description!");
        String description = sc.nextLine();
        task.setDescription(description);
        task.setStatus(updateStatus());
        manager.addNewTask(task);
    }

    private void updateEpic(Epic epic) {
        System.out.println("Insert new epic title!");
        String title = sc.nextLine();
        epic.setTitle(title);
        System.out.println("Insert new epic description!");
        String description = sc.nextLine();
        epic.setDescription(description);
        epic.setStatus(updateStatus());
        manager.addNewEpic(epic);
    }

    private void updateSubtask(Subtask subtask) {
        System.out.println("Insert new subtask title!");
        String title = sc.nextLine();
        subtask.setTitle(title);
        System.out.println("Insert new subtask description!");
        String description = sc.nextLine();
        subtask.setDescription(description);
        subtask.setStatus(updateStatus());
        manager.addNewSubtask(subtask);
        ((Epic) manager.getTaskById(subtask.getEpicId())).removeSubtaskIdFromEpicList(subtask.getId());
    }
}