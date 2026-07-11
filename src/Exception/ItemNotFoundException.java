package Exception;

public class ItemNotFoundException extends Exception {
    public ItemNotFoundException() {
        super("Item not found!");
    }
    public ItemNotFoundException(String message) {
        super(message);
    }
}
