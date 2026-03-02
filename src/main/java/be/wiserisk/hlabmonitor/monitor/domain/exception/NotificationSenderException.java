package be.wiserisk.hlabmonitor.monitor.domain.exception;

public class NotificationSenderException extends RuntimeException {
    public NotificationSenderException(Exception exception) {
        super(exception);
    }
}
