package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.rest;

import be.wiserisk.hlabmonitor.monitor.application.port.in.execution.ExecuteCheckUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.in.execution.ExecuteNotificationUseCase;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
public class DebugController {

    private ExecuteCheckUseCase executeCheckUseCase;
    private ExecuteNotificationUseCase executeNotificationUseCase;

    @Operation(summary = "Execute the check for a Target")
    @ApiResponse(responseCode = "202", description = "Accepted")
    @PostMapping("/{targetId}")
    public ResponseEntity<Void> executeByTargetId(@PathVariable String targetId) {
        executeCheckUseCase.executeCheck(new TargetId(targetId));
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "Delete a notification")
    @ApiResponse(responseCode = "202", description = "Accepted")
    @PostMapping("/{notificationId}")
    public ResponseEntity<Void> deleteByNotificationId(@PathVariable Long notificationId) {
        executeNotificationUseCase.deleteByNotificationId(notificationId);
        return ResponseEntity.accepted().build();
    }

}
