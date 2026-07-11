package Exception;


public class WarehouseNotFoundException extends Exception {
    public WarehouseNotFoundException() {
        super("Warehouse not found!");
    }
    public WarehouseNotFoundException(String message) {
        super(message);
    }
}