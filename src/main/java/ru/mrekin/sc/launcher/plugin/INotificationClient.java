package ru.mrekin.sc.launcher.plugin;

import java.util.Properties;

/**
 * Created by MRekin on 02.09.2014.
 */
public interface INotificationClient extends IRemoteStorageClient {

    public boolean loadProperties(Properties props) throws Exception;

    public boolean connect() throws Exception;

    public boolean disconnect() throws Exception;

    //public boolean getApp(String name, String version, FileOutputStream fos) throws Exception;

    public boolean checkConnection() throws Exception;

    public String getPluginName();

    public String getPluginVersion();

    public Properties getDefaultProperties();

    public void setMessageService(INotificationService ins);


}
