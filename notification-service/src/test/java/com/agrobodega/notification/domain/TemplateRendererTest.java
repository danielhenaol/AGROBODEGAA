package com.agrobodega.notification.domain;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemplateRendererTest {

    @Test
    void reemplazaTodasLasVariables() {
        String result = TemplateRenderer.render(
                "Hola {{name}}, lote {{lot}} y de nuevo {{name}}",
                Map.of("name", "Ana", "lot", "07"));
        assertEquals("Hola Ana, lote 07 y de nuevo Ana", result);
    }

    @Test
    void dejaIntactaUnaVariableSinValor() {
        String result = TemplateRenderer.render("Total {{total}}", Map.of());
        assertEquals("Total {{total}}", result);
    }

    @Test
    void sinVariablesDevuelveLaPlantilla() {
        assertEquals("Texto fijo", TemplateRenderer.render("Texto fijo", null));
    }
}
