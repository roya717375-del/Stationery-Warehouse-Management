package Exception;

public class PermissionNotFoundException extends Exception {
    public PermissionNotFoundException() {
        super("Permission not found!");
    }
    public PermissionNotFoundException(String message) {
        super(message);
    }
}