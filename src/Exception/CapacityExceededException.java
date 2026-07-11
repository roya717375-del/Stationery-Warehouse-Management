package Exception;

public class CapacityExceededException extends Exception {
    public CapacityExceededException() {
        super("Warehouse capacity exceeded!");
    }
    public CapacityExceededException(String message) {
        super(message);
    }
}