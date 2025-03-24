package dev.b6k.fds;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;

@ApplicationScoped
public class SimpleDateTimeProvider implements DateTimeProvider {
    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}