package ru.mrekin.sc.launcher.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

/**
 * Created by MRekin on 31.07.2014.
 */
public class FileDriver {

    private ArrayList<Application> appList;
    // private String appRoot = "./apps";
    private String appRoot = LauncherConstants.WorkingDirectory + SettingsManager.getPropertyByName(LauncherConstants.ApplicationDirectory, "./apps");

    public FileDriver() {
        loadAppsSettings();
    }

    public static ArrayList<File> listFiles(String path, String nameContains) throws IOException {

        ArrayList<File> apps = new ArrayList<File>(1);

        File file = new File(path);

        File[] files = file.listFiles();
        if(files != null) {

            for (File f : files) {
                if (f.isDirectory()) {
                    apps.addAll(listFiles(f.getPath(), nameContains));
                } else if (f.getName().contains(nameContains) && f.isFile()) {
                    apps.add(f);
                }
            }
        }
        return apps;
    }

    public ArrayList<Application> getAppList() {
        return this.appList;
    }

    public void loadAppsSettings() {
        appList = new ArrayList<Application>(1);
        ArrayList<File> apps = new ArrayList<File>(1);
        File file = new File(appRoot);

        System.out.println(file.getAbsolutePath());
        if (!file.exists() || !file.isDirectory()) {
            try {

                boolean f = file.mkdir();
            } catch (Exception ioe) {
                System.out.println("Can't create apps directory: " + ioe.getLocalizedMessage());
            }

        } else {
            try {
                apps = listFiles(file.getPath(), ".jar");
                for (File f : apps) {
                    JarFile jar = new JarFile(f);

                    Attributes attr = jar.getManifest().getMainAttributes();
                    Application apl = new Application();
                    String appName = attr.getValue("appName");
                    String appTitle = attr.getValue("appTitle");
                    String appVersion = attr.getValue("appVersion");
                    if (appName == null) appName = "";
                    if (appTitle == null) appTitle = "";
                    if (appVersion == null) appVersion = "";
                    apl.setAppName(appName);
                    apl.setAppTitle(appTitle);
                    apl.setAppVersion(appVersion);
                    apl.setAppPath(f.getPath());
                    appList.add(apl);
                    jar.close();
                }


            } catch (IOException ioe) {
                System.out.println("Can't list files: " + ioe.getLocalizedMessage());
            }
        }


    }

    public FileOutputStream installApp(String name, String version) {
        try {
            String appDir = appRoot + "/" + name;
            String appName = name + "-" + version + ".jar";
            File appD = new File(appRoot + "/" + name);
            if (!appD.exists() || !appD.isDirectory()) {
                try {

                    boolean f = appD.mkdirs();
                } catch (Exception ioe) {
                    System.out.println("Can't create app directory: " + ioe.getLocalizedMessage());
                }
            }

            return new FileOutputStream(appDir + "/" + appName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;

        }
    }

}
