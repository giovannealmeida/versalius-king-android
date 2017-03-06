package br.com.versalius.iking.utils;

/**
 * Created by jn18 on 13/01/2017.
 */

public class CustomException extends Exception {
    public CustomException(String message) {
        super(message);
    }

    public CustomException(Throwable cause) {
        super(cause);
    }

    public CustomException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getMessage() {
        return super.getMessage();
    }
}
