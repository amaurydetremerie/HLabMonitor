package be.wiserisk.hlabmonitor.monitor.infrastructure.config;

import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.notification.NotificationHandlerDelegate;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.notification.NotificationSender;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.Monitoring;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;

import java.util.List;
import java.util.Map;

@Configuration
@EnableIntegration
public class IntegrationConfig {

    @Bean
    public MessageChannel notificationChannel() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow notificationFlow(MessageChannel notificationChannel,
                                            List<NotificationSender> senders,
                                            Monitoring monitoring,
                                            NotificationHandlerDelegate handleNotification) {
        return IntegrationFlow.from(notificationChannel)
                .enrichHeaders(Map.ofEntries(Map.entry("senders", senders), Map.entry("monitoring", monitoring)))
                .handle(handleNotification)
                .get();
    }

    @Bean
    public NotificationHandlerDelegate handleNotification() {
        return new NotificationHandlerDelegate();
    }

}
