package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.rest;

import be.wiserisk.hlabmonitor.monitor.application.port.in.query.GetCheckTargetIdsUseCase;
import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/v1/targets")
@AllArgsConstructor
@Tag(name = "Check Target Id's")
public class CheckTargetsController {
    private final GetCheckTargetIdsUseCase getCheckTargetIdsUseCase;

    @Operation(summary = "All Target Id's")
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = TargetId.class))
            )
    )
    @GetMapping
    public List<TargetId> getAll() {
        return getCheckTargetIdsUseCase.getAllTargetIds();
    }

    @Operation(summary = "Target Id's of one type")
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = TargetId.class))
            )
    )
    @GetMapping("/{monitoringType}")
    public List<TargetId> getAllByType(@PathVariable MonitoringType monitoringType) {
        return getCheckTargetIdsUseCase.getTargetIdByType(monitoringType);
    }

}
