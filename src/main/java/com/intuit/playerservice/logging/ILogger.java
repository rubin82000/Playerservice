package com.intuit.playerservice.logging;

public interface ILogger {
    void info(String message);
    void warn(String message);
    void error(String message, Throwable throwable);
}