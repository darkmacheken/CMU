package pt.ulisboa.tecnico.cmu.exceptions;

public class DriveServiceNullException extends Exception {

    public DriveServiceNullException() {
    }

    public DriveServiceNullException(String message) {
        super(message);
    }

    public DriveServiceNullException(String message, Throwable cause) {
        super(message, cause);
    }

    public DriveServiceNullException(Throwable cause) {
        super(cause);
    }

    public DriveServiceNullException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
