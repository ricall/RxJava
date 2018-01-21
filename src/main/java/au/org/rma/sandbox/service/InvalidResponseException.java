package au.org.rma.sandbox.service;

public class InvalidResponseException extends Exception {
    public InvalidResponseException(String message) {
        super(message);
    }
}
