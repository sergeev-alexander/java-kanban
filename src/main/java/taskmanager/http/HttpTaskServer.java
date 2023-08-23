package taskmanager.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import taskmanager.exceptions.HttpException;
import taskmanager.model.Epic;
import taskmanager.model.Subtask;
import taskmanager.model.Task;
import taskmanager.service.Managers;
import taskmanager.service.TaskManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HttpTaskServer {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private final HttpServer server;
    private final TaskManager manager;
    private static final Gson GSON = Managers.getGson();

    public HttpTaskServer(TaskManager manager) {
        try {
            server = HttpServer.create();
            server.bind(new InetSocketAddress("localhost", 8080), 0);
            startServer();
        } catch (IOException e) {
            throw new HttpException("Unable to create or bind server because " + e.getMessage());
        }
        server.createContext("/tasks", new TasksHandler());
        this.manager = manager;
    }

    public HttpTaskServer() {
        this(Managers.getDefaultTaskManager(URI.create("http://localhost:8078/")));
    }

    public void startServer() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    private class TasksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();
            String requestMethod = exchange.getRequestMethod();
            URI requestURI = exchange.getRequestURI();

            String responseBody;
            int responseCode;
            int id = 0;
            boolean isNew = true;
            String requestBody;
            if (requestURI.getQuery() != null) {
                String[] queryArray = requestURI.getQuery().split("=");
                id = Integer.parseInt(queryArray[1]);
            }
            for (Task task : manager.getAllTasks()) {
                if (task.getId() == id) {
                    isNew = false;
                    break;
                }
            }

            switch (requestPath) {

                case "/tasks":

                    switch (requestMethod) {
                        case "GET":
                            System.out.println(manager.getAllItems());
                            responseBody = GSON.toJson(manager.getAllItems());
                            responseCode = 200;
                            break;
                        case "DELETE":
                            manager.deleteAllItems();
                            responseBody = "All items where deleted!";
                            responseCode = 200;
                            break;
                        default:
                            responseBody = "Wrong request method!";
                            responseCode = 405;
                    }
                    break;

                case "/tasks/history":

                    if (requestMethod.equals("GET")) {
                        responseBody = GSON.toJson(manager.getHistory());
                        responseCode = 200;
                    } else {
                        throw new HttpException("Wrong request method!");
                    }
                    break;

                case "/tasks/priority":

                    if (requestMethod.equals("GET")) {
                        responseBody = GSON.toJson(manager.getPrioritizedTasks());
                        responseCode = 200;
                    } else {
                        responseBody = "Wrong request method!";
                        responseCode = 405;
                    }
                    break;

                case "/tasks/task/":

                    switch (requestMethod) {
                        case "GET":
                            responseBody = GSON.toJson(manager.getTask(id));
                            responseCode = 200;
                            break;
                        case "POST":
                            requestBody = readRequestBodyText(exchange);
                            if (requestBody.isEmpty()) {
                                responseBody = "Request body is empty!";
                                responseCode = 400;
                            } else {
                                Task task = GSON.fromJson(requestBody, Task.class);
                                if (isNew) {
                                    manager.addNewTask(task);
                                    responseBody = "New task was added!";
                                } else {
                                    task.setId(id);
                                    manager.updateTask(task);
                                    responseBody = "Task was updated!";
                                }
                                responseCode = 201;
                            }
                            break;
                        case "DELETE":
                            manager.deleteTaskById(id);
                            responseCode = 200;
                            responseBody = "Task was deleted!";
                            break;
                        default:
                            responseBody = "Wrong request method!";
                            responseCode = 405;
                    }
                    break;

                case "/tasks/epic/":

                    switch (requestMethod) {
                        case "GET":
                            responseBody = GSON.toJson(manager.getEpic(id));
                            responseCode = 200;
                            break;
                        case "POST":
                            requestBody = readRequestBodyText(exchange);
                            if (requestBody.isEmpty()) {
                                responseBody = "Request body is empty!";
                                responseCode = 400;
                            } else {
                                Epic epic;
                                epic = GSON.fromJson(requestBody, Epic.class);
                                if (isNew) {
                                    manager.addNewEpic(epic);
                                    responseBody = "New epic was added!";
                                } else {
                                    epic.setId(id);
                                    manager.updateEpic(epic);
                                    responseBody = "Epic was updated!";
                                }
                                responseCode = 201;
                            }
                            break;
                        case "DELETE":
                            manager.deleteTaskById(id);
                            responseCode = 200;
                            responseBody = "Epic was deleted!";
                            break;
                        default:
                            responseBody = "Wrong request method!";
                            responseCode = 405;
                    }
                    break;

                case "/tasks/subtask/":

                    switch (requestMethod) {
                        case "GET":
                            responseBody = GSON.toJson(manager.getSubtask(id));
                            responseCode = 200;
                            break;
                        case "POST":
                            requestBody = readRequestBodyText(exchange);
                            if (requestBody.isEmpty()) {
                                responseBody = "Request body is empty!";
                                responseCode = 400;
                            } else {
                                Subtask subtask = GSON.fromJson(requestBody, Subtask.class);
                                if (isNew) {
                                    manager.addNewSubtask(subtask);
                                    responseBody = "New subtask was added!";
                                } else {
                                    subtask.setId(id);
                                    manager.updateSubtask(subtask);
                                    responseBody = "Subtask was updated!";
                                }
                                responseCode = 201;
                            }
                            break;
                        case "DELETE":
                            manager.deleteTaskById(id);
                            responseCode = 200;
                            responseBody = "Subtask was deleted!";
                            break;
                        default:
                            responseBody = "Wrong request method!";
                            responseCode = 405;
                    }
                    break;

                default:
                    throw new HttpException("Unknown request path!");
            }

            sendResponse(exchange, responseBody, responseCode);
        }
    }

    private void sendResponse(HttpExchange exchange, String responseBody, int responseCode) throws IOException {
        exchange.sendResponseHeaders(responseCode, 0);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(responseBody.getBytes());
        }
    }

    private String readRequestBodyText(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
    }

}


