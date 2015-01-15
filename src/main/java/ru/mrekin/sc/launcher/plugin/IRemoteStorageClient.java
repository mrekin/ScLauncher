package ru.mrekin.sc.launcher.plugin;

import ru.mrekin.sc.launcher.core.Application;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by MRekin on 02.09.2014.
 */
public interface IRemoteStorageClient {

    public boolean loadProperties(Properties props) throws Exception;

    public boolean connect() throws Exception;

    public boolean disconnect() throws Exception;

    //public boolean getApp(String name, String version, FileOutputStream fos) throws Exception;

    public boolean checkConnection() throws Exception;

    public ArrayList<Application> getAppList() throws Exception;

    public String getPluginName();

    public String getPluginVersion();

    public Properties getFiles(String appPath, String version);

    public InputStream getFile(String appPath, String version, String fileName);

}
