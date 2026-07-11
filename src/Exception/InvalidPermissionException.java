package Exception;

public class InvalidPermissionException extends Exception {
    public InvalidPermissionException() {
        super("Invalid permission!");
    }
    public InvalidPermissionException(String message) {
        super(message);
    }
}