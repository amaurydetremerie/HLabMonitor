package be.wiserisk.hlabmonitor.monitor.application.port.in.execution;

import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;

public interface ExecuteCheckUseCase {
    void executeCheck(TargetId target);
}
