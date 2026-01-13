package be.wiserisk.hlabmonitor.monitor.domain.service;

import be.wiserisk.hlabmonitor.monitor.application.port.in.execution.ExecuteCheckUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.out.CheckTargetPort;
import be.wiserisk.hlabmonitor.monitor.application.port.out.PersistencePort;
import be.wiserisk.hlabmonitor.monitor.domain.model.Target;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetResult;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MonitoringService implements ExecuteCheckUseCase {

    private final CheckTargetPort checkPort;
    private final PersistencePort persistencePort;

    @Override
    public void executeCheck(TargetId targetId) {
        persistencePort.saveResult(getTargetResult(retrieveTarget(targetId)));
    }

    private TargetResult getTargetResult(Target target) {
        return switch (target.type()) {
            case PING -> pingTarget(target);
            case HTTP -> getTarget(target);
            case CERTIFICATE -> verifyCert(target);
            case SPEEDTEST -> throw new UnsupportedOperationException("Not supported yet.");
            //Use dedicated Exception
            default -> throw new RuntimeException("Unsupported target type: " + target.type());
        };
    }

    private Target retrieveTarget(TargetId targetId) {
        return persistencePort.getTarget(targetId);
    }

    private TargetResult pingTarget(Target target) {
        return checkPort.ping(target);
    }

    private TargetResult getTarget(Target target) {
        return checkPort.httpCheck(target);
    }

    private TargetResult verifyCert(Target target) {
        return checkPort.certCheck(target);
    }
}
