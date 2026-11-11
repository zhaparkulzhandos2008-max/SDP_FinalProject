package setup.domain;

/** Thrown when user input or an action violates application rules. */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) { super(message); }
}
