package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.rest;

import be.wiserisk.hlabmonitor.monitor.application.port.in.management.ManageMonitoringConfigUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.in.query.GetCheckStatisticsUseCase;
import be.wiserisk.hlabmonitor.monitor.domain.enums.StatisticType;
import be.wiserisk.hlabmonitor.monitor.domain.model.Statistics;
import be.wiserisk.hlabmonitor.monitor.domain.model.Target;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/management")
@AllArgsConstructor
@Tag(name = "Manage Targets")
public class CheckStatisticsController {

    private final GetCheckStatisticsUseCase getCheckStatisticsUseCase;

    @Operation(summary = "Some statistics about target")
    @GetMapping("/stats")
    public Statistics getStatistics(@RequestParam(required = false) List<StatisticType> statisticTypes) {
        return getCheckStatisticsUseCase.getStatistics(statisticTypes);
    }
}
