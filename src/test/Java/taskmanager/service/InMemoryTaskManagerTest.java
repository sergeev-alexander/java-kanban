package taskmanager.service;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    public InMemoryTaskManager getManager() {
        return (InMemoryTaskManager) Managers.getDefaultTaskManager();
    }
}
