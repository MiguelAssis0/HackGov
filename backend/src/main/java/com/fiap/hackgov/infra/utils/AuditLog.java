package com.fiap.hackgov.infra.utils;

import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class AuditLog {

    public enum Level { INFO, WARN, ERROR }

    public Builder with(Logger log) {
        return new Builder(log);
    }

    public static class Builder {

        private final Logger log;
        private String event;
        private String email;
        private String reason;
        private Level level = Level.INFO;

        private Builder(Logger log) {
            this.log = log;
        }

        public Builder event(String event) {
            this.event = event;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder level(Level level) {
            this.level = level;
            return this;
        }

        public void log() {
            if (email  != null) MDC.put("email",  email);
            if (reason != null) MDC.put("reason", reason);

            String message = "event=" + event;

            switch (level) {
                case WARN  -> log.warn(message);
                case ERROR -> log.error(message);
                default    -> log.info(message);
            }

            if (email  != null) MDC.remove("email");
            if (reason != null) MDC.remove("reason");
        }
    }
}