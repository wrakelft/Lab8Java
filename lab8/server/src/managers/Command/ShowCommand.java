package managers.Command;

import Collections.Vehicle;
import exceptions.WrongArgumentException;
import managers.CollectionManager;
import managers.Receiver;
import system.Request;

import java.util.HashSet;
import java.util.Set;

/**
 * Данная команда выводит все элементы коллекции
 *
 * @see BaseCommand
 * @author wrakelft
 * @since 1.0
 */
public class ShowCommand implements BaseCommand{


    @Override
    public String execute(Request request) throws Exception{
        throw new UnsupportedOperationException("Use executeRequest() instead");
    }

    @Override
    public Request executeRequest(Request request) throws Exception{
        try {
            return new Request("Command successful", null, null, request.getName(), request.getPasswd(), CollectionManager.getInstance().showCollection(), null);
        } catch (Exception e) {
            return new Request(e.getMessage(), null, null, request.getName(), request.getPasswd(), null, null);
        }
    }

    @Override
    public String getName() {
        return "show ";
    }

    @Override
    public String getDescription() {
        return "show data";
    }
}
