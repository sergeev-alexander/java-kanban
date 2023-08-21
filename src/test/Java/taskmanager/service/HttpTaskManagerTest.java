package taskmanager.service;

import java.io.IOException;
import java.net.URI;

public class HttpTaskManagerTest extends TaskManagerTest<HttpTaskManager> {

    @Override
    public HttpTaskManager getManager() throws IOException, InterruptedException {
        return (HttpTaskManager) Managers.getDefaultTaskManager(URI.create("http://localhost:8078/"));
    }
}
