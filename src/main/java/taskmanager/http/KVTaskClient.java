package taskmanager.http;

import taskmanager.exceptions.HttpException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
public class KVTaskClient {
    private String apiToken;
    private final String URL;


    public KVTaskClient(String serverURL) throws IOException, InterruptedException {
        this.URL = serverURL;
        register();
    }

    public void register() throws IOException, InterruptedException {
        String path = "register";
        URI uri = URI.create(URL + path);
        HttpRequest request = HttpRequest
                .newBuilder()
                .GET()
                .uri(uri)
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        apiToken = response.body();
    }

    public void put(String key, String json) {
        URI url = URI.create(URL + "save/" + key + "?apiToken=" + apiToken);
        HttpRequest request = HttpRequest
                .newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .uri(url)
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException | InterruptedException e) {
            throw new HttpException("Failed to save data!");
        }
    }

    public String load(String key) {
        URI url = URI.create(this.URL + "load/" + key + "?apiToken=" + apiToken);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new HttpException("Failed to load data!");
        }
    }

}
