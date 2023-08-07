package taskmanager.service;

import taskmanager.model.Task;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    public InMemoryTaskManager getManager() {
        return (InMemoryTaskManager) Managers.getDefaultTaskManager();
    }
}
