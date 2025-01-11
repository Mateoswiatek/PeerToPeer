package pl.agh.logger;

public interface ILogger {
    void log(LogLevel level, String message);
    void info(String message);
    void warn(String message);
    void error(String message);
    void debug(String message);
    void trace(String message);
}
