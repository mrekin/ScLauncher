package ru.mrekin.sc.launcher.core;

public interface ISCLogger {
    default void log(String msg) {
        log(msg, "INFO");
    }
    default void log(String msg, String logLevel){
        SCLogger.getInstance().log(this.getClass().getName(), logLevel, msg);
    }
}
