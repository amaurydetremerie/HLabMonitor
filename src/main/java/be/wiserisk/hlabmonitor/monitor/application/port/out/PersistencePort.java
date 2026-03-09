package be.wiserisk.hlabmonitor.monitor.application.port.out;

import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult;
import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType;
import be.wiserisk.hlabmonitor.monitor.domain.model.*;

import java.util.List;
import java.util.Optional;

public interface PersistencePort {
    TargetResult saveResult(TargetResult targetResult);

    Target getTarget(TargetId targetId);

    List<TargetResult> getAllTargetResults();

    PageResponse<TargetResult> getAllResultsFilteredBy(CheckResultsFilter filter, PageRequest pageRequest);

    List<TargetResult> getAllTargetResultsByTargetId(TargetId targetId);

    boolean isTargetIdExist(TargetId targetId);

    void updateTarget(Target target);

    void createTarget(Target target);

    List<Target> getAllTargets(List<TargetId> allActiveTargets);

    List<TargetId> getAllTargetIds();

    List<TargetId> getAllTargetIdsByMonitoringType(MonitoringType monitoringType);

    Long countTarget();

    Long countTarget(MonitoringType monitoringType);

    Long countLast24hResults();

    Long countLast24hResults(MonitoringResult monitoringResult);

    Optional<Notification> getSendNotification(TargetId targetId);

    TargetResult getLastTargetResult(TargetId targetId);

    boolean isResultChanged(TargetId targetId);

    Notification saveNotification(Notification notification);

    List<Notification> getActiveNotifications();

    Integer countActiveNotifications();

    void deleteByNotificationId(Long notificationId);
}
