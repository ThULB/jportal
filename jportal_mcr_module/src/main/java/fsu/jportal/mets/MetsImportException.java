package fsu.jportal.mets;

public class MetsImportException extends Exception {

    private static final long serialVersionUID = 1L;

    public MetsImportException(String message) {
        super(message);
    }

    public MetsImportException(Throwable cause) {
        super(cause);
    }

    public MetsImportException(String message, Throwable cause) {
        super(message, cause);
    }

}
