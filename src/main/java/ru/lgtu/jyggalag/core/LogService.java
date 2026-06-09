package ru.lgtu.jyggalag.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Логирование (системные события и аудит действий пользователя).
 */
public class LogService {
    private static final Logger systemLogger = LoggerFactory.getLogger("SYSTEM");
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    private LogService() {
        throw new IllegalStateException("Utility class");
    }

    public static void info(String message) {
        systemLogger.info(message);
    }

    public static void info(String message, Object... args) {
        systemLogger.info(message, args);
    }

    public static void warn(String message) {
        systemLogger.warn(message);
    }

    public static void warn(String message, Object... args) {
        systemLogger.warn(message, args);
    }

    public static void error(String message) {
        systemLogger.error(message);
    }

    public static void error(String message, String object, Throwable throwable) {
        systemLogger.error(message, object, throwable);
    }

    public static void error(String message, String object, String throwable) {
        systemLogger.error(message, object, throwable);
    }

    public static void error(String message, Throwable throwable) {
        systemLogger.error(message, throwable);
    }

    public static void error(String message, String throwable) {
        systemLogger.error(message, throwable);
    }

    private static final List<String> sessionReportEntries = new ArrayList<>();

    public static void userAction(String username, String action) {
        String logMessage = String.format("[%s] Пользователь '%s': %s", LocalDateTime.now(), username, action);
        auditLogger.info("User '{}': {}", username, action);
        sessionReportEntries.add(logMessage);
    }

    public static List<String> getSessionReportEntries() {
        return new ArrayList<>(sessionReportEntries);
    }
}