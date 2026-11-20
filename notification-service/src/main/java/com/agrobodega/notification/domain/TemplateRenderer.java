package com.agrobodega.notification.domain;

import java.util.Map;

/** Reemplaza los marcadores {{nombre}} de una plantilla por sus valores. */
public final class TemplateRenderer {

    private TemplateRenderer() {
    }

    public static String render(String template, Map<String, String> variables) {
        if (template == null || variables == null) {
            return template;
        }
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String value = entry.getValue() == null ? "" : entry.getValue();
            result = result.replace("{{" + entry.getKey() + "}}", value);
        }
        return result;
    }
}
