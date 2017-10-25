package br.com.j2.apm;

/**
 * Created by jair on 27/12/15.
 */
public class APMException extends RuntimeException {
    public APMException() {
    }

    public APMException(String detailMessage) {
        super(detailMessage);
    }

    public APMException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public APMException(Throwable throwable) {
        super(throwable);
    }
}
