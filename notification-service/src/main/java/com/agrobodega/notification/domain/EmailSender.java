package com.agrobodega.notification.domain;

/** Puerto de salida: cualquier proveedor de correo implementa esta interfaz. */
public interface EmailSender {

    void send(String to, String subject, String body);
}
