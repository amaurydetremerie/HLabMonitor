package be.wiserisk.hlabmonitor.monitor.application.port.out;

import be.wiserisk.hlabmonitor.monitor.domain.model.Target;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetResult;

public interface PersistencePort {
    void saveResult(TargetResult targetResult);

    Target getTarget(TargetId targetId);
}
