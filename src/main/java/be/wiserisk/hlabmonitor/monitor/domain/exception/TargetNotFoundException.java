package be.wiserisk.hlabmonitor.monitor.domain.exception;

import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;

public class TargetNotFoundException extends RuntimeException {
    public TargetNotFoundException(TargetId targetId) {
        super(targetId.id() + "NOT FOUND");
    }
}
