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
    ADAPTER_IN_REST("..monitor.infrastructure.adapter.in.rest.."),
    ADAPTER_IN_NOTIFICATION("..monitor.infrastructure.adapter.in.notification.."),
    ADAPTER_OUT("..monitor.infrastructure.adapter.out.."),
    ADAPTER_OUT_PERSISTENCE("..monitor.infrastructure.adapter.out.persistence.."),
    ADAPTER_OUT_PERSISTENCE_CONVERTER("..monitor.infrastructure.adapter.out.persistence.converter.."),
    ADAPTER_OUT_PERSISTENCE_ENTITY("..monitor.infrastructure.adapter.out.persistence.entity.."),
    ADAPTER_OUT_PERSISTENCE_REPOSITORY("..monitor.infrastructure.adapter.out.persistence.repository.."),
    ADAPTER_OUT_SCHEDULER("..monitor.infrastructure.adapter.out.scheduler.."),
    CONFIG("..monitor.infrastructure.config.."),
    CONFIG_MAPPER("..monitor.infrastructure.config.mapper.."),
    CONFIG_YAML("..monitor.infrastructure.config.yaml.."),

    STD_JAVA("java.."),
    STD_LOMBOK("lombok..");

    private final String stringPackage;

    AppEnum(String stringPackage) {
        this.stringPackage = stringPackage;
    }
}