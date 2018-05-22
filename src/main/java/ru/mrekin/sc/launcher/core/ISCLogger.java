package ru.mrekin.sc.launcher.core;

import java.lang.invoke.MethodHandles;

public  interface ISCLogger {
    default void log(String msg){
        //SCLogger.getInstance().log(MethodHandles.lookup().lookupClass().getName(),"INFO",msg);
        SCLogger.getInstance().log(this.getClass().getName(),"INFO",msg);
    }
}
