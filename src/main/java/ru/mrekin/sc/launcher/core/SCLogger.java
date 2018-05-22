package ru.mrekin.sc.launcher.core;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class SCLogger {

    public static String getLogFileName() {
        return LogFileName;
    }

    private static String LogFileName = "log.txt";

    static SCLogger instance = null;

    /**
     *
     */
    private SCLogger() {
        init();
    }

    /**
     *
     */
    private void init() {
        Logger rootLog = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        FileAppender fa = (FileAppender) rootLog.getAppender("FILE");
        LogFileName = fa.getFile();
        log(MethodHandles.lookup().lookupClass().getName(), "INFO", "SetUp logger to: " + LogFileName);
    }

    /**
     * @return
     */
    public static SCLogger getInstance() {
        if (instance == null) {
            instance = new SCLogger();
        }

        return instance;
    }

    /**
     * @param className
     * @param level
     * @param message
     */
    public void log(String className, String level, String message) {
        Logger log = (Logger) LoggerFactory.getLogger(className);
        log.info(message);
    }


}
