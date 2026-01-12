package be.wiserisk.hlabmonitor.monitor.application.port.out;

import be.wiserisk.hlabmonitor.monitor.domain.model.Target;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetResult;

public interface CheckTargetPort {
    TargetResult ping(Target target);
    TargetResult httpCheck(Target target);
    TargetResult certCheck(Target target);
}
