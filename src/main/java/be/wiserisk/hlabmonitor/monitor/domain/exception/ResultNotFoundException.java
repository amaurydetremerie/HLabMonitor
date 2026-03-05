package be.wiserisk.hlabmonitor.monitor.domain.exception;

import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;

public class ResultNotFoundException extends RuntimeException {
    public ResultNotFoundException(TargetId targetId) {
        super("RESULT FOR " + targetId.id() + " NOT FOUND");
    }
}
