package fsu.jportal.backend.gnd;

/**
 * Something went wrong while querying the GNDLocationService.
 */
public class GNDLocationServiceException extends RuntimeException {

    public GNDLocationServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
