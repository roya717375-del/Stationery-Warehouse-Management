package Exception;

public class InsufficientCashException extends Exception {
    public InsufficientCashException() {
        super("Insufficient cash balance!");
    }
    public InsufficientCashException(String message) {
        super(message);
    }
}