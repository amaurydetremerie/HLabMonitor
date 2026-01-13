package be.wiserisk.hlabmonitor.monitor.domain.model;

import java.util.List;

public record PageResponse<T>(List<T> content,
                              int page,
                              int size,
                              long totalElements,
                              boolean hasNext) {
}