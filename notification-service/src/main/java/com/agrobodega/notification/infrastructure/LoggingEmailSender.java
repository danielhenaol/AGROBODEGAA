package com.agrobodega.notification.infrastructure;

import com.agrobodega.notification.domain.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

/** Modo local: sin RESEND_API_KEY solo escribe el correo en el log. */
@Component
@ConditionalOnExpression("'${resend.api-key:}'.isBlank()")
public class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    @Override
    public void send(String to, String subject, String body) {
        log.warn("RESEND_API_KEY no configurada: correo simulado.\nPara: {}\nAsunto: {}\n{}", to, subject, body);
    }
}
