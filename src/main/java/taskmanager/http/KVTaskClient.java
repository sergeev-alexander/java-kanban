package taskmanager.http;

import taskmanager.exceptions.HttpException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class KVTaskClient {
    private final String apiToken;
    private final String url;


    public KVTaskClient(String serverURL) {
        this.url = serverURL;
        this.apiToken = register();
    }

    private String register() {
        String path = "register";
        URI uri = URI.create(url + path);
        HttpRequest request = HttpRequest
                .newBuilder()
                .GET()
                .uri(uri)
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new HttpException("response.statusCode() != 200");
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new HttpException("Failed to register!");
        }
    }

    public void put(String key, String json) {
        URI url = URI.create(this.url + "save/" + key + "?apiToken=" + apiToken);
        HttpRequest request = HttpRequest
                .newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .uri(url)
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() != 200) {
                throw new HttpException("response.statusCode() != 200");
            }
        } catch (IOException | InterruptedException e) {
            throw new HttpException("Failed to save data!");
        }
    }

    public String load(String key) {
        URI url = URI.create(this.url + "load/" + key + "?apiToken=" + apiToken);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new HttpException("response.statusCode() != 200");
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new HttpException("Failed to load data!");
        }
    }

}
