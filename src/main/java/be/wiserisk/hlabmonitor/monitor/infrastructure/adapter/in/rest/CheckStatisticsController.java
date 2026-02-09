package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.rest;

import be.wiserisk.hlabmonitor.monitor.application.port.in.query.GetCheckStatisticsUseCase;
import be.wiserisk.hlabmonitor.monitor.domain.enums.StatisticType;
import be.wiserisk.hlabmonitor.monitor.domain.model.Statistics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/management")
@AllArgsConstructor
@Tag(name = "Manage Targets")
public class CheckStatisticsController {

    private final GetCheckStatisticsUseCase getCheckStatisticsUseCase;

    @Operation(summary = "Some statistics about target")
    @ApiResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Statistics.class)
            )
    )
    @GetMapping("/stats")
    public Statistics getStatistics(@RequestParam(required = false) List<StatisticType> statisticTypes) {
        return getCheckStatisticsUseCase.getStatistics(statisticTypes);
    }
}
