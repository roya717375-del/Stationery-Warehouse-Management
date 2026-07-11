package Exception;


public class InventoryNotFoundException extends Exception {
    public InventoryNotFoundException() {
        super("Inventory not found!");
    }
    public InventoryNotFoundException(String message) {
        super(message);
    }
}
