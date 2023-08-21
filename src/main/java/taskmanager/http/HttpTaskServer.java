package taskmanager.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import taskmanager.exceptions.HttpException;
import taskmanager.model.AdapterLocalDateTime;
import taskmanager.model.Epic;
import taskmanager.model.Subtask;
import taskmanager.model.Task;
import taskmanager.service.Managers;
import taskmanager.service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class HttpTaskServer {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private final HttpServer server;
    private static final TaskManager manager;

    static {
        try {
            manager = Managers.getDefaultTaskManager(URI.create("http://localhost:8078/"));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new AdapterLocalDateTime())
            .setPrettyPrinting()
            .create();

    public HttpTaskServer() throws IOException {
        server = HttpServer.create();
        server.bind(new InetSocketAddress("localhost", 8080), 0);
        server.createContext("/tasks", new TasksHandler());
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    private static class TasksHandler implements HttpHandler {
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
            InputStream inputStream = exchange.getRequestBody();
            requestBody = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);

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
                            throw new HttpException("Wrong request method!");
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
                        throw new HttpException("Wrong request method!");
                    }
                    break;

                case "/tasks/task/":

                    switch (requestMethod) {
                        case "GET":
                            responseBody = GSON.toJson(manager.getTask(id));
                            responseCode = 200;
                            break;
                        case "POST":
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
                            break;
                        case "DELETE":
                            manager.deleteTaskById(id);
                            responseCode = 200;
                            responseBody = "Task was deleted!";
                            break;
                        default:
                            throw new HttpException("Wrong request method");
                    }
                    break;

                case "/tasks/epic/":

                    switch (requestMethod) {
                        case "GET":
                            responseBody = GSON.toJson(manager.getEpic(id));
                            responseCode = 200;
                            break;
                        case "POST":
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
                            break;
                        case "DELETE":
                            manager.deleteTaskById(id);
                            responseCode = 200;
                            responseBody = "Epic was deleted!";
                            break;
                        default:
                            throw new HttpException("Wrong request method");
                    }
                    break;

                case "/tasks/subtask/":

                    switch (requestMethod) {
                        case "GET":
                            responseBody = GSON.toJson(manager.getSubtask(id));
                            responseCode = 200;
                            break;
                        case "POST":
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
                            break;
                        case "DELETE":
                            manager.deleteTaskById(id);
                            responseCode = 200;
                            responseBody = "Subtask was deleted!";
                            break;
                        default:
                            throw new HttpException("Wrong request method!");
                    }
                    break;

                default:
                    throw new HttpException("Unknown request path!");
            }

            System.out.println("\nNEW REQUEST");
            System.out.println("Request uri: " + requestURI);
            System.out.println("Request method: " + requestMethod);
            System.out.println("Request path: " + requestPath);
            System.out.println("Id : " + id);
            System.out.println("Request body: " + requestBody);
            System.out.println("Response body: " + responseBody);
            System.out.println("Response code: " + responseCode);


            exchange.sendResponseHeaders(responseCode, 0);

            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(responseBody.getBytes());
            }
        }
    }
}


