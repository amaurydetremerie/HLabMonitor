package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.rest;

import be.wiserisk.hlabmonitor.monitor.application.port.in.query.GetCheckResultsUseCase;
import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult;
import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType;
import be.wiserisk.hlabmonitor.monitor.domain.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/results")
@AllArgsConstructor
@Tag(name = "Check Results")
public class CheckResultsController {
    private final GetCheckResultsUseCase getCheckResultsUseCase;

    @Operation(summary = "All target results")
    @GetMapping
    public List<TargetResult> getAll() {
        return getCheckResultsUseCase.getAllResults();
    }

    @Operation(summary = "Results of one target")
    @GetMapping("/{targetId}")
    public List<TargetResult> getAllByTargetId(@PathVariable String targetId) {
        return getCheckResultsUseCase.getTargetIdResults(new TargetId(targetId));
    }

    @Operation(summary = "All target results filtered")
    @GetMapping("/search")
    public PageResponse<TargetResult> getAllFiltered(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant to,
            @RequestParam(required = false, defaultValue = "") List<String> targetIdList,
            @RequestParam(required = false) List<MonitoringResult> monitoringResultList,
            @RequestParam(required = false) List<MonitoringType> monitoringTypeList,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "0") int page) {
        CheckResultsFilter filter = new CheckResultsFilter(from, to, targetIdList.stream().map(TargetId::new).toList(), monitoringResultList, monitoringTypeList);
        PageRequest pageRequest = new PageRequest(page, size);
        return getCheckResultsUseCase.getFilteredResults(filter, pageRequest);
    }

}
