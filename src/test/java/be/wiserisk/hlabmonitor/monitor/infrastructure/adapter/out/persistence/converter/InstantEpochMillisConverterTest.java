package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.converter;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class InstantEpochMillisConverterTest {

    public static final long EPOCH_MILLI = 123456789L;
    public static final Instant INSTANT = Instant.ofEpochMilli(EPOCH_MILLI);
    InstantEpochMillisConverter instantEpochMillisConverter = new InstantEpochMillisConverter();

    @Test
    void convertToDatabaseColumn() {
        assertThat(instantEpochMillisConverter.convertToDatabaseColumn(INSTANT)).isNotNull().isEqualTo(EPOCH_MILLI);
    }

    @Test
    void convertToEntityAttribute() {
        assertThat(instantEpochMillisConverter.convertToEntityAttribute(EPOCH_MILLI)).isNotNull().isEqualTo(INSTANT);
    }

    @Test
    void convertToDatabaseColumnNull() {
        assertThat(instantEpochMillisConverter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void convertToEntityAttributeNull() {
        assertThat(instantEpochMillisConverter.convertToEntityAttribute(null)).isNull();
    }
}