package ru.mrekin.sc.launcher.core;

// This class implemented only for one purpose - use declared interface (may be this is bad way)
public class SettingsFactory {
    private static ISettingsManager instance = null;

    public static ISettingsManager getInstance(){
        if(instance==null){
            instance = new SettingsManagerTypeSafe();
        }
        return instance;
    }

}
