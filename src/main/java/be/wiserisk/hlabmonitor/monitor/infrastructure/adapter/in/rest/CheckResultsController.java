package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.rest;

import be.wiserisk.hlabmonitor.monitor.application.port.in.query.GetCheckResultsUseCase;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetResult;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

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

}
