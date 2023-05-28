package Resources;

public class WrongTableException extends Exception {
    public WrongTableException() {
        super("Wrong Table.");
    }

    public WrongTableException(String message) {
        super(message);
    }
}