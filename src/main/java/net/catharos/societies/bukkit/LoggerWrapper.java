package net.catharos.societies.bukkit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.spi.AbstractLogger;

import java.util.logging.Logger;

/**
 * Represents a LoggerWrapper
 */
public class LoggerWrapper extends AbstractLogger {
    private final Logger logger;

    public LoggerWrapper(Logger logger) {this.logger = logger;}

    @Override
    public boolean isEnabled(Level level, Marker marker, Message message, Throwable t) {
        return true;
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, Object message, Throwable t) {
        return true;
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Throwable t) {
        return true;
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message) {
        return true;
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object... params) {
        return true;
    }

    @Override
    public void logMessage(String string, Level level, Marker marker, Message message, Throwable t) {
        if (level == Level.ALL) {
            logger.log(java.util.logging.Level.ALL, message.getFormattedMessage(), t);
        } else if (level == Level.DEBUG) {
            logger.log(java.util.logging.Level.FINEST, message.getFormattedMessage(), t);
        } else if (level == Level.ERROR) {
            logger.log(java.util.logging.Level.SEVERE, message.getFormattedMessage(), t);
        } else if (level == Level.FATAL) {
            logger.log(java.util.logging.Level.SEVERE, message.getFormattedMessage(), t);
        } else if (level == Level.INFO) {
            logger.log(java.util.logging.Level.INFO, message.getFormattedMessage(), t);
        } else if (level == Level.OFF) {
            logger.log(java.util.logging.Level.OFF, message.getFormattedMessage(), t);
        } else if (level == Level.TRACE) {
            logger.log(java.util.logging.Level.SEVERE, message.getFormattedMessage(), t);
        } else if (level == Level.WARN) {
            logger.log(java.util.logging.Level.WARNING, message.getFormattedMessage(), t);
        } else {
            logger.log(java.util.logging.Level.INFO, message.getFormattedMessage(), t);
        }
    }


    @Override
    public Level getLevel() {
        return Level.INFO;
    }
}
