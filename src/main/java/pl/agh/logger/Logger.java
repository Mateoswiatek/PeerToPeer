package pl.agh.logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger implements ILogger {

    private static final String LOG_FILE_PATH = "app.log";

    private Logger() { }

    private static class SingletonHelper {
        private static final Logger SINGLETON_INSTANCE = new Logger();
    }

    public static Logger getInstance() {
        return Logger.SingletonHelper.SINGLETON_INSTANCE;
    }

    @Override
    public void log(LogLevel level, String message) {
        String timestamp = getTimestamp();
        String logMessage = String.format("[%s] [%s] %s", timestamp, level, message);

        writeColoredLogToConsole(level, logMessage);

        writeToLogFile(logMessage);
    }

    private String getTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return now.format(formatter);
    }

    private void writeColoredLogToConsole(LogLevel level, String logMessage) {
        System.out.println(getColoredLogMessage(level, logMessage));
    }
    private String getColoredLogMessage(LogLevel level, String logMessage) {
        String color = switch (level) {
            case LogLevel.INFO -> "\033[32m";  // Green
            case LogLevel.WARN -> "\033[33m";  // Yellow
            case LogLevel.ERROR -> "\033[31m";  // Red
            case LogLevel.DEBUG -> "\033[34m";  // Blue
            case LogLevel.TRACE -> "\033[36m";  // Cyan
            default -> "\033[0m";  // Default (no color)
        };

        return color + logMessage + "\033[0m";
    }

    private void writeToLogFile(String logMessage) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE_PATH, true))) {
            writer.write(logMessage);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void info(String message) {
        log(LogLevel.INFO, message);
    }

    @Override
    public void warn(String message) {
        log(LogLevel.WARN, message);
    }

    @Override
    public void error(String message) {
        log(LogLevel.ERROR, message);
    }

    @Override
    public void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    @Override
    public void trace(String message) {
        log(LogLevel.TRACE, message);
    }
}