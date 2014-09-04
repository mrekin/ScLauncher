package ru.mrekin.sc.launcher.core;

import ru.mrekin.sc.launcher.SvnClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

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

    public static AppManager getInstance(){
        if(instance!=null){

            return instance;
        }else{
            return new AppManager();
        }
    }

    public void init(){
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

    public void updateApplication(String appName) {

        String version = "";
        for (Application app : svnClient.getAppList()) {
            if (app.getAppName().equals(appName)) {
                version = app.getAppLastVersion();
            }
        }
        installApplication(appName, version);

    }

    public void installApplication(String appName, String version) {

        if (!svnClient.checkSvnConnection()) {
            System.out.println("Can't access to SVN, check connection");
            return;
        }
        FileOutputStream fs = fileDriver.installApp(appName, version);

        boolean b = svnClient.getApp(appName, version, fs);
        System.out.println("Copied " + b);
        loadLocalAppInfo();
        try {
            fs.close();
            fs = null;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


    public void deleteApplication(String appName) {

        String path = "";
        for (Application app : fileDriver.getAppList()) {
            if (app.getAppName().equals(appName)) {
                path = app.getAppPath();
                break;
            }
        }
        File f = new File(path);
        System.out.println(f.delete());
    }


}
