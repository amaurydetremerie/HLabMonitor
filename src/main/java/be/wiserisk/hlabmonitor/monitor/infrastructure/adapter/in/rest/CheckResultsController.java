package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.rest;

import be.wiserisk.hlabmonitor.monitor.application.port.in.query.GetCheckResultsUseCase;
import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult;
import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType;
import be.wiserisk.hlabmonitor.monitor.domain.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/v1/results")
@AllArgsConstructor
@Tag(name = "Check Results")
public class CheckResultsController {
    private final GetCheckResultsUseCase getCheckResultsUseCase;

    @Operation(summary = "All target results")
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = TargetResult.class))
            )
    )
    @GetMapping
    public List<TargetResult> getAll() {
        return getCheckResultsUseCase.getAllResults();
    }

    @Operation(summary = "Results of one target")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = TargetResult.class))
            )
    )
    @ApiResponse(responseCode = "404", description = "Not Found",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/{targetId}")
    public List<TargetResult> getAllByTargetId(@PathVariable String targetId) {
        return getCheckResultsUseCase.getTargetIdResults(new TargetId(targetId));
    }

    @Operation(summary = "All target results filtered")
    @ApiResponse(
            responseCode = "200",
            description = "OK"
    )
    @GetMapping(value = "/search", produces = APPLICATION_JSON_VALUE)
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
