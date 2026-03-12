package be.wiserisk.hlabmonitor.archunit;

import lombok.Getter;

@Getter
public enum AppEnum {
    MONITOR("..monitor.."),

    DOMAIN("..monitor.domain.."),
    DOMAIN_ENUM("..monitor.domain.enums.."),
    DOMAIN_EXCEPTION("..monitor.domain.exception.."),
    DOMAIN_MODEL("..monitor.domain.model.."),
    DOMAIN_SERVICE("..monitor.domain.service.."),

    APPLICATION("..monitor.application.."),
    PORTS("..monitor.application.port.."),
    PORTS_IN("..monitor.application.port.in.."),
    PORTS_OUT("..monitor.application.port.out.."),

    INFRASTRUCTURE("..monitor.infrastructure.."),
    ADAPTER("..monitor.infrastructure.adapter.."),
    ADAPTER_IN("..monitor.infrastructure.adapter.in.."),
    ADAPTER_OUT("..monitor.infrastructure.adapter.out.."),
    CONFIG("..monitor.infrastructure.config.."),

    STD_JAVA("java.."),
    STD_LOMBOK("lombok..");

    private final String stringPackage;

    AppEnum(String stringPackage) {
        this.stringPackage = stringPackage;
    }
}