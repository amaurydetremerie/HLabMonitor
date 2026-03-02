package be.wiserisk.hlabmonitor.monitor.infrastructure.config;

import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.notification.*;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.rest.CheckNotificationController;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.Monitoring;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.SmtpConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class NotificationConfig {

    @Bean
    public SseSender sseSender(CheckNotificationController checkNotificationController) {
        return new SseSender(checkNotificationController);
    }

    @Bean
    public JavaMailSender javaMailSender(Monitoring monitoring) {
        SmtpConfig smtpConfig = monitoring.getNotification().notificationEmail().smtp();

        if(smtpConfig == null ||
                smtpConfig.host() == null || smtpConfig.host().isEmpty() ||
                smtpConfig.port() <= 0 ||
                smtpConfig.username() == null || smtpConfig.username().isEmpty() ||
                smtpConfig.password() == null || smtpConfig.password().isEmpty()){
            return null;
        }

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(smtpConfig.host());
        sender.setPort(smtpConfig.port());
        sender.setUsername(smtpConfig.username());
        sender.setPassword(smtpConfig.password());
        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", smtpConfig.auth());
        props.put("mail.smtp.starttls.enable", smtpConfig.tls());
        props.put("mail.transport.protocol", "smtp");

        return sender;
    }

    @Bean
    public EmailSender emailSender(Monitoring monitoring, JavaMailSender javaMailSender) {
        if((javaMailSender == null || monitoring.getNotification() == null) ||
                !monitoring.getNotification().enabled() ||
                (monitoring.getNotification().notificationEmail() == null) ||
                !monitoring.getNotification().notificationEmail().enabled() ||
                (monitoring.getNotification().notificationEmail().from() == null) ||
                monitoring.getNotification().notificationEmail().from().isEmpty() ||
                (monitoring.getNotification().notificationEmail().to() == null) ||
                monitoring.getNotification().notificationEmail().to().isEmpty()){
            return null;
        }
        return new EmailSender(javaMailSender,
                monitoring.getNotification().notificationEmail().from(),
                monitoring.getNotification().notificationEmail().to());
    }

    @Bean
    public TelegramSender telegramSender(Monitoring monitoring, ObjectMapper objectMapper) {
        if(monitoring.getNotification() == null ||
                !monitoring.getNotification().enabled() ||
                monitoring.getNotification().notificationTelegram() == null ||
                !monitoring.getNotification().notificationTelegram().enabled() ||
                monitoring.getNotification().notificationTelegram().token() == null ||
                monitoring.getNotification().notificationTelegram().token().isEmpty() ||
                monitoring.getNotification().notificationTelegram().chatId() == null ||
                monitoring.getNotification().notificationTelegram().chatId().isEmpty()){
            return null;
        }
        return new TelegramSender(
                objectMapper,
                monitoring.getNotification().notificationTelegram().token(),
                monitoring.getNotification().notificationTelegram().chatId());
    }

    @Bean
    public DiscordSender discordSender(Monitoring monitoring, ObjectMapper objectMapper) {
        if(monitoring.getNotification() == null ||
                !monitoring.getNotification().enabled() ||
                monitoring.getNotification().notificationDiscord() == null ||
                !monitoring.getNotification().notificationDiscord().enabled() ||
                monitoring.getNotification().notificationDiscord().webhookUrl() == null ||
                monitoring.getNotification().notificationDiscord().webhookUrl().isEmpty()){
            return null;
        }
        return new DiscordSender(
                objectMapper,
                monitoring.getNotification().notificationDiscord().webhookUrl());
    }

    @Bean
    public LogSender logSender(Monitoring monitoring) {
        if(monitoring.getNotification() == null ||
                !monitoring.getNotification().enabled() ||
                monitoring.getNotification().notificationLog() == null ||
                !monitoring.getNotification().notificationLog().enabled() ||
                monitoring.getNotification().notificationLog().level() == null){
            return null;
        }
        return new LogSender(monitoring.getNotification().notificationLog().level());
    }
}
