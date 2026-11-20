package com.agrobodega.notification.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record SendNotificationRequest(
        @NotBlank String eventCode,
        String channel,
        @NotBlank @Email String to,
        Map<String, String> variables) {

    public String channelOrDefault() {
        return (channel == null || channel.isBlank()) ? "EMAIL" : channel;
    }
}
