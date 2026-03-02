package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.rest;

import be.wiserisk.hlabmonitor.monitor.application.port.in.query.GetCheckNotificationsUseCase;
import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/v1/notifications")
@AllArgsConstructor
@Tag(name = "Check Notifications")
public class CheckNotificationController {

    private final GetCheckNotificationsUseCase getCheckNotificationsUseCase;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @Operation(summary = "All active notifications")
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = Notification.class))
            )
    )
    @GetMapping
    public List<Notification> getActiveNotifications() {
        return getCheckNotificationsUseCase.getActiveNotifications();
    }

    @Operation(summary = "count all active notifications")
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Integer.class)
            )
    )
    @GetMapping("/count")
    public Integer countActiveNotifications() {
        return getCurrentCount();
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        ExecutorService sseMvcExecutor = Executors.newSingleThreadExecutor();
        sseMvcExecutor.execute(() -> sendCountToEmitter(emitter));

        return emitter;
    }

    public void broadcastDbCount() {
        List<SseEmitter> dead = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                sendCountToEmitter(emitter);
            } catch (Exception e) {
                dead.add(emitter);
            }
        }

        emitters.removeAll(dead);
        dead.forEach(SseEmitter::complete);
    }

    private void sendCountToEmitter(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .name("notifications-count-update")
                    .data(getCurrentCount()));
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    private Integer getCurrentCount() {
        return getCheckNotificationsUseCase.countActiveNotifications();
    }
}
