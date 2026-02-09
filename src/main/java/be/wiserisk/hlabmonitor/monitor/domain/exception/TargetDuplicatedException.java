package be.wiserisk.hlabmonitor.monitor.domain.exception;

public class TargetDuplicatedException extends RuntimeException {
    public TargetDuplicatedException(String message) {
        super(message);
    }

    public TargetDuplicatedException() {
        super("Duplicated Target found");
    }
}
