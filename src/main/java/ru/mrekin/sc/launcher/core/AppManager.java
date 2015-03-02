package ru.mrekin.sc.launcher.core;

import org.apache.commons.io.FileUtils;
import ru.mrekin.sc.launcher.gui.AppInstallForm;
import ru.mrekin.sc.launcher.gui.LauncherGui;
import ru.mrekin.sc.launcher.plugin.IRemoteStorageClient;
import ru.mrekin.sc.launcher.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

//import ru.mrekin.sc.launcher.SvnClient;

/**
 * Created by MRekin on 03.08.2014.
 */
public class AppManager {

    //, svnAppList;
    private static AppManager instance;
    //FileDriver FileDriver.getInstance();
    //SvnClient svnClient;
    IRemoteStorageClient client;
    ArrayList<Application> appList;

    private AppManager() {
        init();
        instance = this;
    }

    public static AppManager getInstance() {
        if (instance != null) {

            return instance;
        } else {
            return new AppManager();
        }
    }

    public void init() {
       // this.FileDriver.getInstance() = FileDriver.getInstance();
//        this.svnClient = new SvnClient();
        appList = FileDriver.getInstance().getAppList();
        updateAppList();
//        svnAppList = svnClient.getAppList();

    }

    /*   public SvnClient getSvnClient() {
           return svnClient;
       }
   */
 /*   public void setSvnClient(SvnClient svnClient) {
        this.svnClient = svnClient;
    }
*/
    /*public FileDriver getFileDriver() {
        return fileDriver;
    }
*/
    /*
    public void setFileDriver(FileDriver fileDriver) {
        this.fileDriver = fileDriver;
    }
*/

    public void loadLocalAppInfo() {
        FileDriver.getInstance().loadAppsSettings();
    }

    public ArrayList<Application> getAppList() {
        updateAppList();
        return appList;
    }

    /**
     * Method fill appList by apps from remote storages
     */
    public void updateAppList() {
        ArrayList<Plugin> plugins = PluginManager.getInstance().getPlugins();

        for (Plugin pl : plugins) {

            if (pl.isInstalled()) {
                ArrayList<Application> apps = new ArrayList<Application>(1);
                try {
                    apps = pl.getPluginObj().getAppList();
                } catch (Exception e) {
                    System.out.println(e.getLocalizedMessage());
                }
                for (Application app : apps) {
                    app.setSourcePlugin(pl.getPluginName());
                    //Check if app installed (already in list. May be later will check by isInstalled
                    if (appList.contains(app)) {
                        //If app already installed - set avaliable versions and sourcePlugin
                        appList.get(appList.indexOf(app)).setAppVersions(app.getAppVersions());
                        appList.get(appList.indexOf(app)).setSourcePlugin(app.getSourcePlugin());
                    } else {
                        //If not installed - add to list
                        appList.add(app);
                    }

                }
            }
        }

        return;
    }

    /*    public void setAppList(ArrayList<Application> appList) {
            this.appList = appList;
        }
    */
/*    public ArrayList<Application> getSvnAppList() {
        return svnAppList;
    }
*/
/*    public void setSvnAppList(ArrayList<Application> svnAppList) {
        this.svnAppList = svnAppList;
    }
*/
    public void updateApplication(String appPath) {

        String version = "";
        String name = "";

        for (Application app : appList) {
            if (app.getAppPath().equals(appPath) && !"".equals(app.getSourcePlugin())) {
                version = app.getAppLastVersion();
                name = app.getAppName();
                break;
            }
        }
        deleteApplication(appPath);
        installApplication(name, version);

    }

    private Application getAppByName(String name) {

        for (Application app : appList) {
            if (name.equals(app.getAppName())) {
                return app;
            }
        }
        return null;
    }

    public void installApplication(String appName, String version) {


        client = PluginManager.getInstance().getPluginByName(getAppByName(appName).getSourcePlugin()).getPluginObj();
        try {
            if (!client.checkConnection()) {
                System.out.println("Can't access to " + client.getPluginName() + " storage, check connection");
                return;
            }
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
        try {
            client.connect();
        }catch (Exception e){
            e.printStackTrace();
        }
        class MyThread extends Thread {
            boolean run = true;

            int i = 0;
            AppInstallForm iform = new AppInstallForm();
            String appName, version, appPath;


            public void run() {
                while (run) {

                    //FileOutputStream fs = fileDriver.installApp(appName, version);

                    Properties files = client.getFiles(appPath, version);
                    InputStream is;
                    try {
                        int i = 0;
                        for (String fileName : files.stringPropertyNames()) {
                            if (iform != null) {
                                iform.setValues(i, files.size());
                                iform.update();
                            }
                            is = client.getFile(appPath, version, fileName);
                            if (!FileDriver.getInstance().installFile("", appPath, version, fileName, is)) {
                                System.out.println("Can't install file: " + fileName);
                            }
                            is.close();
                            i++;
                        }
                    } catch (IOException ioe) {
                        System.out.println(ioe.getLocalizedMessage());
                    }

                    //boolean b = svnClient.getApp(appName, version, fs);
                    //System.out.println("Copied " + b);
                    loadLocalAppInfo();
                   /* try {
                        //fs.close();
                        //fs = null;
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }*/

                    try {
                        sleep(600);
                    } catch (Exception e) {
                        e.getLocalizedMessage();
                    }
                    iform.dispose();
                    LauncherGui.getInstance().init();
                    LauncherGui.getInstance().launch();
                    client = null;
                    stopT();
                }
            }

            public void stopT() {
                run = false;
            }


            public void setData(String appName, String version) {

                this.appName = appName;
                this.version = version;
                appPath = getAppByName(appName).getAppPath();

            }
        }

        MyThread th = new MyThread();
        th.setData(appName, version);
        th.start();

    }


    public void deleteApplication(String appPath) {
        //TODO need not remove local settings file / update settings when updating application
        //TODO need to move this to FileDriver
        String path = "";
        if ("".equals(appPath) || appPath == null) {
            System.out.print("Nothing to delete. Ok.");
            return;
        }
        for (Application app : FileDriver.getInstance().getAppList()) {
            if (app.getAppPath().equals(appPath)) {
                path = LauncherConstants.WorkingDirectory + SettingsManager.getPropertyByName(LauncherConstants.ApplicationDirectory) + app.getAppPath();
                break;
            }
        }
        File f = new File(path);
        try {
            FileUtils.deleteDirectory(f);
        } catch (IOException ioe) {
            System.out.println(ioe.getLocalizedMessage());
        }
    }


}
