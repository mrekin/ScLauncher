package ru.mrekin.sc.launcher.core;

public interface ISettingsManager extends ISCLogger{

    public String getPropertyByName(String name);
    public String getPropertyByName(String name, String defValue);
    public String[] getPropertiesArrayByName(String name, String defValue);
    public String[] getPropertiesArrayByName(String name);
    public boolean updateLocalSettings();
    public void loadLauncherSettings();
    public void loadLauncherProperties();
    public void loadPluginProperties();
    public void save();


}
