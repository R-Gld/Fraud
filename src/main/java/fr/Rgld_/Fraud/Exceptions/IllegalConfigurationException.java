package fr.Rgld_.Fraud.Exceptions;

public class IllegalConfigurationException extends RuntimeException {

    /**
     * Constructs a new runtime exception with {@code null} as its
     */
    public IllegalConfigurationException() {
        super();
    }

    /**
     * @param message the message to display
     */
    public IllegalConfigurationException(String message) {
        super(message);
    }

    /**
     * @param message the message to display
     * @param cause the cause of the exception
     */
    public IllegalConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause the cause of the exception
     */
    public IllegalConfigurationException(Throwable cause) {
        super(cause);
    }
}
