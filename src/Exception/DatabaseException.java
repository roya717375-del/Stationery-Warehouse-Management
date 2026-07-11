package Exception;


public class DatabaseException extends Exception {
    public DatabaseException() {
        super("Database error!");
    }
    public DatabaseException(String message) {
        super(message);
    }
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}