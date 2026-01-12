package be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommonTest {

    @Test
    void calculateIntervalEmpty() {
        assertThat(Common.calculateInterval(null, null)).isNotNull().isEqualTo(Common.DEFAULT_DURATION);
    }

}