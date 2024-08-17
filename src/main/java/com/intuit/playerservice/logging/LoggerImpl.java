package com.intuit.playerservice.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggerImpl implements ILogger {
    private final Logger logger;

    public LoggerImpl() {
        this.logger = LoggerFactory.getLogger(LoggerImpl.class);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }
}