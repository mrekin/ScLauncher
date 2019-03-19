package ru.mrekin.sc.launcher.core;

import org.apache.commons.io.FileUtils;
import ru.mrekin.sc.launcher.gui.AppInstallForm;
import ru.mrekin.sc.launcher.gui.ConfirmationForm;
import ru.mrekin.sc.launcher.gui.LauncherGui;
import ru.mrekin.sc.launcher.plugin.INotificationClient;
import ru.mrekin.sc.launcher.plugin.IRemoteStorageClient;
import ru.mrekin.sc.launcher.plugin.Plugin;
import ru.mrekin.sc.launcher.tools.ApplicationPrepare;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

//import ru.mrekin.sc.launcher.SvnClient;

/**
 * Created by MRekin on 03.08.2014.
 */
public class AppManager implements ISCLogger {

    //, svnAppList;
    private static AppManager instance;
    //FileDriver FileDriver.getInstance();
    //SvnClient svnClient;
    IRemoteStorageClient client;
    // ArrayList<Application> appList;
    CopyOnWriteArrayList<Application> appList;

    private AppManager() {
        instance = this;
        init();

    }
/*
    private static void log(String msg) {
        SCLogger.getInstance().log(AppManager.class.getName(), "INFO", msg);
    }
*/

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
        FileDriver.getInstance().loadAppsSettings();
        appList = FileDriver.getInstance().getAppList();
        //      appList = (ArrayList<Application>) Collections.synchronizedList(appList);
        updateAppList();
//        svnAppList = svnClient.getAppList();

    }


    public void loadLocalAppInfo() {
        FileDriver.getInstance().loadAppsSettings();
        appList = FileDriver.getInstance().getAppList();
        log("Local appList loaded: " + appList.size());
    }

    public void loadLocalAppInfo2() {
        CopyOnWriteArrayList<Application> apps = new CopyOnWriteArrayList<Application>();

        FileDriver.getInstance().loadAppsSettings();
        apps = FileDriver.getInstance().getAppList();
        log("Local appList loaded (v2): " + apps.size());

        for (Application app : apps) {
            if (appList.contains(app)) {
                //If app already installed - set avaliable versions and sourcePlugin
                appList.get(appList.indexOf(app)).setAppVersion(app.getAppVersion());
                appList.get(appList.indexOf(app)).setInstalled(true);
                log("App updated: " + app.getAppName() + ". Added versions: " + app.getAppVersions());
            } else {
                //If not installed - add to list
                appList.add(app);
            }

        }
        for (Application app : appList) {
            if (app.isInstalled() && apps.indexOf(app) == -1) {
                appList.remove(app);
                log("Removed from applist: "+app.getAppName(),"DEBUG");
            }
        }
    }

    public ArrayList<Application> getAppList() {
        // updateAppList();
        return new ArrayList(appList);
    }

    /**
     * Method fill appList by apps from remote storages
     */
    public void updateAppList() {

        ArrayList<Plugin> plugins = PluginManager.getInstance().getPlugins();
        Runnable task2 = () -> {
            for (Plugin pl : plugins) {


                if (pl.isInstalled() && pl.getPluginObj() instanceof IRemoteStorageClient && !(pl.getPluginObj() instanceof INotificationClient)) {
                    ArrayList<Application> apps = new ArrayList<Application>(1);
                    try {
                        pl.getPluginObj().connect();
                        apps = pl.getPluginObj().getAppList();
                        log("Plugin '" + pl.getPluginName() + "' connected.");
                        log("Applist loaded. Size " + apps.size());
                    } catch (Exception e) {
                        log(e.getLocalizedMessage());
                    }
                    for (Application app : apps) {
                        app.setSourcePlugin(pl.getPluginName());
                        //Check if app installed (already in list. May be later will check by isInstalled
                        if (appList.contains(app)) {
                            //If app already installed - set avaliable versions and sourcePlugin
                            appList.get(appList.indexOf(app)).setAppVersions(app.getAppVersions());
                            appList.get(appList.indexOf(app)).setSourcePlugin(app.getSourcePlugin());
                            log("App updated: " + app.getAppName() + ". Added versions: " + app.getAppVersions());
                        } else {
                            //If not installed - add to list
                            appList.add(app);
                        }

                    }
                }
                //AppManager.getInstance().updateAppList();


            }
            LauncherGui.getInstance().launch();
        };
        new Thread(task2).start();

        return;
    }


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
        if (deleteApplication(appPath, true, "Current version will be deleted!")) {
            installApplication(name, version);
        }
        // AppManager.getInstance().updateAppList();
        //LauncherGui.getInstance().launch();

    }

    private Application getAppByName(String name) {

        for (Application app : appList) {
            if (name.equals(app.getAppName())) {
                log("GetAppByName: "+app.getAppName(),"DEBUG");
                return app;
            }
        }
        return null;
    }

    public void installApplication(String appName, String version) {

        log("Installing: " + appName + " " + version);
        try {
            client = PluginManager
                    .getInstance()
                    .getPluginByName(getAppByName(appName)
                            .getSourcePlugin())
                    .getPluginObj();
            log("GetPlugin return: "+client.getPluginName(),"DEBUG");
        }catch (Exception e){
            log("GetPlugin failed: "+ e.getLocalizedMessage(),"DEBUG");
        }
        try {
            if (!client.checkConnection()) {
                try {
                    client.connect();
                    log("Install application: Connected to repo.","DEBUG");
                } catch (Exception e) {
                    e.printStackTrace();
                    log("Can't access to " + client.getPluginName() + " storage, check connection");
                    return;
                }

            }
        } catch (Exception e) {
            log(e.getLocalizedMessage());
        }
    /*    try {
            client.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        class MyThread extends Thread {
            boolean run = true;

            int i = 0;
            AppInstallForm iform = new AppInstallForm();
            String appName, version, appPath;


            public void run() {
                while (run) {

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
                                log("Can't install file: " + fileName);
                            }
                            is.close();
                            i++;
                        }
                    } catch (IOException ioe) {
                        log(ioe.getLocalizedMessage());
                    }


                    try {
                        sleep(600);
                    } catch (Exception e) {
                        e.getLocalizedMessage();
                    }
                    iform.dispose();

                    //                 LauncherGui.getInstance().launch();
                    client = null;
                    stopT();
                }
            }

            public void stopT() {
                run = false;
                AppManager.getInstance().loadLocalAppInfo();
                AppManager.getInstance().updateAppList();
            }


            public void setData(String appName, String version) {

                this.appName = appName;
                this.version = version;
                appPath = getAppByName(appName).getAppPath();


            }
        }

        MyThread th = new MyThread();
        th.setName("AppInstaller");
        th.setData(appName, version);
        log("Install application: Starting thread","DEBUG");
        th.start();

    }


    public boolean deleteApplication(String appPath, boolean needConfirmation, String confirmationText) {
        //TODO need not remove local settings file / update settings when updating application
        //TODO need to move this to FileDriver

        String path = "";
        if ("".equals(appPath) || appPath == null) {
            log("Nothing to delete. Ok.");
            return true;
        }
        for (Application app : FileDriver.getInstance().getAppList()) {
            if (app.getAppPath().equals(appPath)) {
                if (needConfirmation && app.isInstalled()) {
                    ConfirmationForm cf = new ConfirmationForm();
                    cf.setText(confirmationText);
                    if (!cf.launch()) {
                        log("Deleting not confirmed","DEBUG");
                        return false;
                    }
                }
                path = LauncherConstants.WorkingDirectory + SettingsManager.getInstance().getPropertyByName(LauncherConstants.ApplicationDirectory) + app.getAppPath();
                break;
            }
        }
        File f = new File(path);
        try {
            FileUtils.deleteDirectory(f);
            log("Application deleted","DEBUG");
        } catch (IOException ioe) {
            log("Exception on deleting application: "+ioe.getLocalizedMessage());
            return false;
        }
        //loadLocalAppInfo();
        AppManager.getInstance().loadLocalAppInfo2();
        AppManager.getInstance().updateAppList();
        return true;
    }

    public void createAppLink(ShellLinkEx link, String appName, String appVersion, String appType) {

        String path = "";
        if ("".equals(link.resolveTarget()) || link == null) {
            System.out.print("Nothing to create. Ok.");
            return;
        }
        /*
        for (Application app : FileDriver.getInstance().getAppList()) {
            if (app.getAppPath().equals(appPath)) {
                path = LauncherConstants.WorkingDirectory + SettingsManager.getInstance().getPropertyByName(LauncherConstants.ApplicationDirectory) + app.getAppPath();
                break;
            }
        }*/
        File f = new File(SettingsManager.getInstance().getPropertyByName(LauncherConstants.ApplicationDirectory) + "/" + appName);
        if (f.exists()) {
            log("App (" + f.getAbsolutePath() + ") already exists");
            //return;
        }
        try {
            FileUtils.forceMkdir(f);
        } catch (IOException ioe) {
            log(ioe.getLocalizedMessage());
        }

        //save link to target dir
        try {
            link.saveTo(f.getAbsolutePath() + "/" + appName + ".lnk");
        } catch (IOException ioe) {
            log(ioe.toString());
        }
        ApplicationPrepare.appPrepare(f.getAbsolutePath(), appName, appVersion, appType, appName + ".lnk");
        //loadLocalAppInfo();
        AppManager.getInstance().loadLocalAppInfo();
        AppManager.getInstance().updateAppList();
    }


}
