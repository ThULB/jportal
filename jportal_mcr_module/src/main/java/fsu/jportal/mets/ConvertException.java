package fsu.jportal.mets;

/**
 * Is thrown for converting failures.
 * 
 * @author Matthias Eichner
 */
public class ConvertException extends Exception {

    public ConvertException(String message) {
        super(message);
    }

    public ConvertException(Throwable cause) {
        super(cause);
    }

    public ConvertException(String message, Throwable cause) {
        super(message, cause);
    }
}
