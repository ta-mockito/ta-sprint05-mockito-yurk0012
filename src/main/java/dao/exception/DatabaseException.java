package dao.exception;

/**
 * Exception thrown when a database operation fails.
 * This is a runtime exception that wraps the underlying SQLException.
 */
public class DatabaseException extends RuntimeException {
    
    /**
     * Constructs a new DatabaseException with the specified detail message.
     *
     * @param message the detail message
     */
    public DatabaseException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new DatabaseException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new DatabaseException with the specified cause.
     *
     * @param cause the cause
     */
    public DatabaseException(Throwable cause) {
        super(cause);
    }
}