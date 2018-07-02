package eventprocessing.customerrors;

public class InvalidSqsResponseException extends RuntimeException {
    public InvalidSqsResponseException(Throwable cause) {
        super(cause);
    }
}
