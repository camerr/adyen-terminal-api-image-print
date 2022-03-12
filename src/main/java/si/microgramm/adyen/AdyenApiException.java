package si.microgramm.adyen;

public class AdyenApiException extends Exception {

    public AdyenApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdyenApiException(Throwable cause) {
        super(cause);
    }
}
