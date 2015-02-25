package ru.mrekin.sc.launcher.plugin;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * Created by xam on 21.01.2015.
 */
public interface IPluginRepoClient {
    /**
     * @param user - username
     * @param pass - password
     */
    public void connect(String user, String pass, String serverURL) throws Exception;

    /**
     * Method returns list of avaliable plugins
     *
     * @return
     */
    public List<Plugin> getPluginsList();

    /**
     * Method install plugin
     *
     * @param pluginSimpleName
     * @param version
     */
    public InputStream getPluginIS(String pluginSimpleName, String version) throws Exception;

    public Properties getDefaultProperties();

    public String getRepoPluginName();

    public String getRepoPluginVersion();

}
