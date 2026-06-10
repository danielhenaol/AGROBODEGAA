package com.agrobodega.crosscutting.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(MdcLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String method = request.getMethod();
        String uri = request.getRequestURI();

        MDC.put("traceId", traceId);
        MDC.put("method", method);
        MDC.put("uri", uri);

        response.setHeader("X-Trace-Id", traceId);

        long startTime = System.currentTimeMillis();
        logger.info("REQUEST [{} {}] traceId={}", method, uri, traceId);

        try {
            filterChain.doFilter(request, response);
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            if (status >= 400) {
                logger.error("RESPONSE [{} {}] status={} duration={}ms traceId={}",
                        method, uri, status, duration, traceId);
            } else {
                logger.info("RESPONSE [{} {}] status={} duration={}ms traceId={}",
                        method, uri, status, duration, traceId);
            }
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("ERROR [{} {}] exception={} duration={}ms traceId={}",
                    method, uri, ex.getMessage(), duration, traceId);
            throw ex;
        } finally {
            MDC.clear();
        }
    }
}