package ru.mrekin.sc.launcher.plugin;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by MRekin on 02.09.2014.
 */
public class Plugin {

    private static String pluginName = "";
    private String pluginSimpleName = "";
    private String pluginVersion = "";
    private HashMap<String, String> pluginVersions = null;
    private URL pluginPath;
    private IRemoteStorageClient pluginObj = null;
    private boolean installed = false;
    private String repository = "";


    public static String getPluginName() {
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

    public IRemoteStorageClient getPluginObj() {
        return pluginObj;
    }

    public void setPluginObj(IRemoteStorageClient pluginObj) {
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

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getPluginSimpleName() {
        return pluginSimpleName;
    }

    public void setPluginSimpleName(String pluginSimpleName) {
        this.pluginSimpleName = pluginSimpleName;
    }

    public String getLatestVersion() {

        if (this.pluginVersions != null && this.pluginVersions.size() != 0) {
            ArrayList<String> listVersions = new ArrayList<String>(this.pluginVersions.keySet());
            Collections.sort(listVersions);

            return listVersions.get(listVersions.size() - 1);
        } else {
            return "-1";
        }


    }

    @Override
    public boolean equals(Object plugin) {
        if (!(plugin instanceof Plugin)) {
            return false;
        }
        Plugin pl = (Plugin) plugin;
        if (this.pluginName.equals(pl.getPluginName()) || this.pluginSimpleName.equals(pl.getPluginName()) || this.pluginName.equals(pl.getPluginSimpleName()) || this.pluginSimpleName.equals(pl.getPluginSimpleName())) {
            if (pl.getPluginVersions() != null) {
                for (String ver : this.pluginVersions.keySet()) {
                    if (!pl.getPluginVersions().containsKey(ver)) {
                        HashMap<String, String> temp = pl.getPluginVersions();
                        temp.put(ver, this.pluginVersions.get(ver));
                        pl.setPluginVersions(temp);
                    }
                }

            } else {
                pl.setPluginVersions(this.pluginVersions);
            }

            return true;
        }
        return false;
    }
}
