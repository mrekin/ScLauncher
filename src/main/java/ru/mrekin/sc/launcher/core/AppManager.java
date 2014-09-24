package ru.mrekin.sc.launcher.core;

import org.apache.commons.io.FileUtils;
import ru.mrekin.sc.launcher.SvnClient;
import ru.mrekin.sc.launcher.gui.AppInstallForm;
import ru.mrekin.sc.launcher.gui.LauncherGui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by MRekin on 03.08.2014.
 */
public class AppManager {

    FileDriver fileDriver;
    SvnClient svnClient;
    ArrayList<Application> appList, svnAppList;
    static AppManager instance;

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
        this.fileDriver = new FileDriver();
        this.svnClient = new SvnClient();
        appList = fileDriver.getAppList();
        svnAppList = svnClient.getAppList();

    }

    public SvnClient getSvnClient() {
        return svnClient;
    }

    public void setSvnClient(SvnClient svnClient) {
        this.svnClient = svnClient;
    }

    public FileDriver getFileDriver() {
        return fileDriver;
    }

    public void setFileDriver(FileDriver fileDriver) {
        this.fileDriver = fileDriver;
    }


    public void loadLocalAppInfo() {
        fileDriver.loadAppsSettings();
    }

    public ArrayList<Application> getAppList() {
        return appList;
    }

    public void setAppList(ArrayList<Application> appList) {
        this.appList = appList;
    }

    public ArrayList<Application> getSvnAppList() {
        return svnAppList;
    }

    public void setSvnAppList(ArrayList<Application> svnAppList) {
        this.svnAppList = svnAppList;
    }

    public void updateApplication(String appPath) {

        String version = "";
        for (Application app : svnClient.getAppList()) {
            if (app.getAppPath().equals(appPath)) {
                version = app.getAppLastVersion();
            }
        }
        deleteApplication(appPath);
        installApplication(appPath, version);

    }

    public void installApplication(String appName, String version) {

        if (!svnClient.checkSvnConnection()) {
            System.out.println("Can't access to SVN, check connection");
            return;
        }


        class MyThread extends Thread {
            boolean run = true;

            int i = 0;
            AppInstallForm iform = new AppInstallForm();
            String appName, version;

            public void run() {
                while (run) {

                    //FileOutputStream fs = fileDriver.installApp(appName, version);

                    Properties files = svnClient.getFiles(appName, version);
                    InputStream is;
                    try {
                        int i = 0;
                        for (String fileName : files.stringPropertyNames()) {
                            if (iform != null) {
                                iform.setValues(i, files.size());
                                iform.update();
                            }
                            is = svnClient.getFile(appName, version, fileName);
                            if (!fileDriver.installFile(appName, version, fileName, is)) {
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
                    stopT();
                }
            }

            public void stopT() {
                run = false;
            }


            public void setData(String appName, String version) {

                this.appName = appName;
                this.version = version;


            }
        }

        MyThread th = new MyThread();
        th.setData(appName, version);
        th.start();

    }


    public void deleteApplication(String appPath) {
        //TODO need not remove local settings file / update settings when updating application
        String path = "";
        for (Application app : fileDriver.getAppList()) {
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
