package taskmanager;

import taskmanager.http.KVServer;

public class Main {

    public static void main(String[] args) {

        KVServer kvServer = new KVServer();
        kvServer.start();
        Interaction action = new Interaction();
        action.interaction();

    }
}
