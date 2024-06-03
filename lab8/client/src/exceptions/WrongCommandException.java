package exceptions;

public class WrongCommandException extends Exception{
    public WrongCommandException(String line){
        super("Wrong command: " + line);
    }
}
