/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;

import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreferences;
import com.mucommander.ui.dialog.debug.DebugConsoleAppender;

/**
 * This class manages logging issues within mucommander
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class MuLogging {

	/** Levels of log printings */
	public enum LogLevel {
		OFF(0),
		SEVERE(1),
		WARNING(2),
		INFO(3),
		CONFIG(4),
		FINE(5),
		FINER(6),
		FINEST(7);

		private int value;

		LogLevel(int value) {
			this.value = value;
		}

		public int value() {
			return value;
		}
	}
	
	/** Appender that writes log printings to the standard console */
	private static ConsoleAppender<ILoggingEvent> consoleAppender;
	
	/** Appender that writes log printings to the debug console dialog */
	private static DebugConsoleAppender debugConsoleAppender;

	/**
	 * Sets the level of all muCommander loggers.
	 *
	 * @param level the new log level
	 */
	private static void updateLogLevel(LogLevel level) {
		// TODO: re-implement that with the new logging API.
		ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

		ch.qos.logback.classic.Level logbackLevel = null;
		switch (level) {
		case OFF:
			logbackLevel = ch.qos.logback.classic.Level.OFF;
			break;
		case SEVERE:
			logbackLevel = ch.qos.logback.classic.Level.ERROR;
			break;
		case WARNING:
			logbackLevel = ch.qos.logback.classic.Level.WARN;
			break;
		case INFO:
		case CONFIG:
			logbackLevel = ch.qos.logback.classic.Level.INFO;
			break;
		case FINE:
		case FINER:
			logbackLevel = ch.qos.logback.classic.Level.DEBUG;
			break;
		case FINEST:
			logbackLevel = ch.qos.logback.classic.Level.TRACE;
			break;
		}

		logger.setLevel(logbackLevel);
	}
	
	public static LogLevel getLevel(ILoggingEvent lr) {
    	switch(lr.getLevel().toInt()) {
    	case ch.qos.logback.classic.Level.OFF_INT:
    		return LogLevel.OFF;
    	case ch.qos.logback.classic.Level.ERROR_INT:
    		return LogLevel.SEVERE;
    	case ch.qos.logback.classic.Level.WARN_INT:
    		return LogLevel.WARNING;
    	case ch.qos.logback.classic.Level.INFO_INT:
    		return LogLevel.INFO;
    	case ch.qos.logback.classic.Level.DEBUG_INT:
    		return LogLevel.FINE;
    	case ch.qos.logback.classic.Level.TRACE_INT:
    		return LogLevel.FINEST;
    	default:
    		return LogLevel.OFF;
    	}
    }


	/**
	 * Returns the current log level used by all <code>java.util.logging</code> loggers.
	 *
	 * @return the current log level used by all <code>java.util.logging</code> loggers.
	 */
	public static LogLevel getLogLevel() {
		return LogLevel.valueOf(MuConfigurations.getPreferences().getVariable(MuPreferences.LOG_LEVEL, MuPreferences.DEFAULT_LOG_LEVEL));
	}


	/**
	 * Sets the new log level to be used by all <code>java.util.logging</code> loggers, and persists it in the
	 * application preferences.
	 *
	 * @param level the new log level to be used by all <code>java.util.logging</code> loggers.
	 */
	public static void setLogLevel(LogLevel level) {
		MuConfigurations.getPreferences().setVariable(MuPreferences.LOG_LEVEL, level.toString());
		updateLogLevel(level);
	}
	
	public static DebugConsoleAppender getDebugConsoleAppender() {
		return debugConsoleAppender;
	}
	
	public static ConsoleAppender<ILoggingEvent> getConsoleAppender() {
		return consoleAppender;
	}

	static void configureLogging() throws IOException {
		// We're no longer using LogManager and a logging.properties file to initialize java.util.logging, because of
		// a limitation with Webstart limiting the use of handlers and formatters residing in the system's classpath,
		// i.e. built-in ones.

		// Get root logger
		ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		
		// we are not interested in auto-configuration
		LoggerContext loggerContext = rootLogger.getLoggerContext();
		loggerContext.reset();
		
		// Remove default appenders
		rootLogger.detachAndStopAllAppenders();
		
		// and add ours
		Appender<ILoggingEvent>[] appenders = createAppenders(loggerContext);
		for (Appender<ILoggingEvent> appender : appenders)
			rootLogger.addAppender(appender);
		
		// Set the log level to the value defined in the configuration
		updateLogLevel(getLogLevel());
	}
	
	private static Appender<ILoggingEvent>[] createAppenders(LoggerContext loggerContext) {
		Layout<ILoggingEvent> layout = new CustomLoggingLayout();

		consoleAppender = createConsoleAppender(loggerContext, layout);
		debugConsoleAppender = createDebugConsoleAppender(loggerContext, layout);
		
		return new Appender[] { consoleAppender, debugConsoleAppender };
	}

	private static ConsoleAppender<ILoggingEvent> createConsoleAppender(LoggerContext loggerContext, Layout<ILoggingEvent> layout) {
		ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<ILoggingEvent>();

		LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<ILoggingEvent>();
		encoder.setContext(loggerContext);
	    encoder.setLayout(layout);
	    encoder.start();

	    consoleAppender.setContext(loggerContext);
	    consoleAppender.setEncoder(encoder);
	    consoleAppender.start();

	    return consoleAppender;
	}
	
	private static DebugConsoleAppender createDebugConsoleAppender(LoggerContext loggerContext, Layout<ILoggingEvent> layout) {
		DebugConsoleAppender debugConsoleAppender = new DebugConsoleAppender(layout);
		
		debugConsoleAppender.setContext(loggerContext);
		debugConsoleAppender.start();
		
		return debugConsoleAppender;
	}

	private static class CustomLoggingLayout extends LayoutBase<ILoggingEvent> {

		private final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		
		public String doLayout(ILoggingEvent event) {
			StackTraceElement stackTraceElement = event.getCallerData()[0];
			
			StringBuffer sbuf = new StringBuffer(128);
			sbuf.append("[");
			sbuf.append(SIMPLE_DATE_FORMAT.format(new Date(event.getTimeStamp())));
			sbuf.append("] ");
			sbuf.append(getLevel(event));
			sbuf.append(" ");
			sbuf.append(stackTraceElement.getFileName());
			sbuf.append("#");
			sbuf.append(stackTraceElement.getMethodName());
			sbuf.append(",");
			sbuf.append(stackTraceElement.getLineNumber());
			sbuf.append(" ");
			sbuf.append(event.getFormattedMessage());
			sbuf.append(CoreConstants.LINE_SEPARATOR);
			return sbuf.toString();
		}
	}
}
