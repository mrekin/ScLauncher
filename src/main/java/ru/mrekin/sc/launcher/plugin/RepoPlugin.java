package ru.mrekin.sc.launcher.plugin;

import java.net.URL;
import java.util.HashMap;

/**
 * Created by MRekin on 02.09.2014.
 */
public class RepoPlugin {

    private String pluginName = "";
    private String pluginVersion = "";
    private HashMap<String, String> pluginVersions = null;
    private URL pluginPath;
    private IPluginRepoClient pluginObj = null;
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

    public IPluginRepoClient getPluginObj() {
        return pluginObj;
    }

    public void setPluginObj(IPluginRepoClient pluginObj) {
        this.pluginObj = pluginObj;
    }

    public URL getPluginPath() {
        return pluginPath;
    }

    public void setPluginPath(URL pluginPath) {
        this.pluginPath = pluginPath;
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean b) {
        installed = b;
    }

    public HashMap<String, String> getPluginVersions() {
        return pluginVersions;
    }

    public void setPluginVersions(HashMap<String, String> pluginVersions) {
        this.pluginVersions = pluginVersions;
    }

    public String getLatestVersion() {
        String maxVer = "";
        for (String version : pluginVersions.keySet()) {
            if (version.compareToIgnoreCase(maxVer) == -1) {
                maxVer = version;
            }
        }
        return maxVer;
    }
}
