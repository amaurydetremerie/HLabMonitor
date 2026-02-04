package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.rest;

import be.wiserisk.hlabmonitor.monitor.application.port.in.query.GetCheckResultsUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.in.query.GetCheckTargetIdsUseCase;
import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType;
import be.wiserisk.hlabmonitor.monitor.domain.model.Target;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/targets")
@AllArgsConstructor
@Tag(name = "Check Target Id's")
public class CheckTargetsController {
    private final GetCheckTargetIdsUseCase getCheckTargetIdsUseCase;

    @Operation(summary = "All Target Id's")
    @GetMapping
    public List<TargetId> getAll() {
        return getCheckTargetIdsUseCase.getAllTargetIds();
    }

    @Operation(summary = "Target Id's of one type")
    @GetMapping("/{monitoringType}")
    public List<TargetId> getAllByType(@PathVariable MonitoringType monitoringType) {
        return getCheckTargetIdsUseCase.getTargetIdByType(monitoringType);
    }

}
