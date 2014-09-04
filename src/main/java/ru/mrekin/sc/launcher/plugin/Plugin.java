package ru.mrekin.sc.launcher.plugin;

import java.net.URL;

/**
 * Created by MRekin on 02.09.2014.
 */
public class Plugin {

    private String pluginName = "";
    private String pluginVersion = "";
    private URL pluginPath;
    private RemoteStorageClient pluginObj = null;
    private boolean installed = false;

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public void setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }

    public RemoteStorageClient getPluginObj() {
        return pluginObj;
    }

    public void setPluginObj(RemoteStorageClient pluginObj) {
        this.pluginObj = pluginObj;
    }

    public URL getPluginPath() {
        return pluginPath;
    }

    public void setPluginPath(URL pluginPath) {
        this.pluginPath = pluginPath;
    }

    public boolean isInstalled(){
        return installed;
    }

    public void setIntalled(boolean b){
        installed = b;
    }
}
