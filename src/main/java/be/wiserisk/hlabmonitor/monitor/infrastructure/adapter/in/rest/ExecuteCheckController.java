package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.rest;

import be.wiserisk.hlabmonitor.monitor.application.port.in.execution.ExecuteCheckUseCase;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/debug/execute")
@AllArgsConstructor
@Tag(name = "Execute a check (debug only)")
public class ExecuteCheckController {

    private ExecuteCheckUseCase executeCheckUseCase;

    @Operation(summary = "Execute the check for a Target")
    @PostMapping("/{targetId}")
    public ResponseEntity<Void> executeByTargetId(@PathVariable String targetId) {
        executeCheckUseCase.executeCheck(new TargetId(targetId));
        return ResponseEntity.accepted().build();
    }

}
