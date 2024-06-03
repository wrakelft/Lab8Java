package managers.Command;

import managers.Receiver;
import system.Request;

public class RemoveCommand implements BaseCommand {
    @Override
    public String execute(Request request) throws Exception {
        try {
            return Receiver.remove(request);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "remove all your changed elements";
    }
}
