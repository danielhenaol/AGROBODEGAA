package com.agrobodega.notification.domain;

public record NotificationTemplate(String eventCode, String channel, String subject, String bodyTemplate) {
}
