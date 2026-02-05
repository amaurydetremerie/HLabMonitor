package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.rest;

import be.wiserisk.hlabmonitor.monitor.application.port.in.management.ManageMonitoringConfigUseCase;
import be.wiserisk.hlabmonitor.monitor.domain.model.Target;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/management")
@AllArgsConstructor
@Tag(name = "Manage Targets")
public class MonitoringManagementController {

    private final ManageMonitoringConfigUseCase manageConfigUseCase;

    @PostMapping
    public ResponseEntity<Void> addTarget(@RequestBody Target target) {
        manageConfigUseCase.saveNewAndRefreshTarget(target);
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/")
    public ResponseEntity<Void> updateTarget(@RequestBody Target target) {
        manageConfigUseCase.updateAndRefreshExistingTarget(target);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{targetId}/stop")
    public ResponseEntity<Void> stopMonitoring(@PathVariable String targetId) {
        manageConfigUseCase.stopMonitoring(new TargetId(targetId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{targetId}/resume")
    public ResponseEntity<Void> resumeMonitoring(@PathVariable String targetId) {
        manageConfigUseCase.resumeMonitoring(new TargetId(targetId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reload")
    public ResponseEntity<Void> reloadConfiguration() {
        manageConfigUseCase.reloadAllMonitoring();
        return ResponseEntity.ok().build();
    }
}
