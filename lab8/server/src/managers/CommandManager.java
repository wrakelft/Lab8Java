package managers;

import exceptions.UnknowCommandException;
import managers.Command.*;
import system.Request;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;

/**
 * Класс обеспечивает связь между командами и CollectionManager
 *
 * @see CollectionManager
 * @author wrakelft
 * @since 1.0
 */
public class CommandManager {
    private static LinkedHashMap<String, BaseCommand> commandList;
    public static ArrayDeque<BaseCommand> lastTwelveCommand = new ArrayDeque<>();

    public CommandManager() {
        commandList = new LinkedHashMap<>();
        commandList.put("help", new HelpCommand());
        commandList.put("info", new InfoCommand());
        commandList.put("show", new ShowCommand());
        commandList.put("removeById", new RemoveById());
        commandList.put("clear", new ClearCommand());
        commandList.put("save", new SaveCommand());
        commandList.put("exit", new ExitCommand());
        commandList.put("add", new AddCommand());
        commandList.put("removeLower", new RemoveLowerCommand());
        commandList.put("updateId", new UpdateIdCommand());
        commandList.put("addIfMax", new AddIfMaxCommand());
        commandList.put("register", new RegisterCommand());
        commandList.put("login", new LoginCommand());
        commandList.put("countByFuelType", new CountByFuelTypeCommand());
        commandList.put("countLessThenFuelType", new CountLessThenFuelTypeCommand());
        commandList.put("groupCountingByCreationDate", new GroupCountingByCreationDateCommand());
        commandList.put("remove", new RemoveCommand());
    }

    /**
     * Выполняет команду, сохраняя ее имя
     *
     * @since 1.0
     */
    public static Request startExecute(Request request) throws Exception {
        String commandName = request.getMessage().split(" ")[0];
        if (!commandList.containsKey(commandName)) {
            throw new UnknowCommandException(commandName);
        }
        BaseCommand command = commandList.get(commandName);
        try {
            return command.executeRequest(request);
        } catch (UnsupportedOperationException e) {
            if (commandName.equals("remove")) {
                String response = command.execute(request);
                return new Request(response, null, null, request.getName(), request.getPasswd(), null, null);
            } else {
                String response = command.execute(request);
                return new Request(response, null, null, request.getName(), request.getPasswd(), CollectionManager.getInstance().showCollection(), null);
            }
        }
    }

    public static Request startExecutingClientMode(Request request) {
        try {
            if(!request.getMessage().equals("exit") && !request.getMessage().equals("save")) {
                return startExecute(request);
            }
            String message =  "Unknown command";
            return new Request(message, null, null, request.getName(), request.getPasswd(), null, null);
        }catch (Exception e) {
            String ermes =  e.getMessage();
            return new Request(ermes, null, null, request.getName(), request.getPasswd(), null, null);
        }
    }

    public static void startExecutingServerMode(Request request) {
        try {
            if(request.getMessage().equals("exit") || request.getMessage().equals("save")) {
                startExecute(request);
            }
        }catch (Exception e) {
            System.out.println("Something wrong with initializing of server");
        }
    }

    public static LinkedHashMap<String, BaseCommand> getCommandList() {
        return commandList;
    }

    }



