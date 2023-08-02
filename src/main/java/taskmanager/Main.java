package taskmanager;

import taskmanager.exceptions.AddingAndUpdatingException;
import taskmanager.exceptions.NoSuchTaskException;

public class Main {

    public static void main(String[] args) {

        try {
            Interaction action = new Interaction();
            action.interaction();
        } catch (NoSuchTaskException | AddingAndUpdatingException e) {
            System.out.println(e.getMessage());
        }
    }

}
