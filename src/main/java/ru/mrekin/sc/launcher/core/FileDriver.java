package ru.mrekin.sc.launcher.core;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
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
        if (files != null) {

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
                //TODO !!! will not work with old version. Need to do something
                apps = listFiles(file.getPath(), "app.properties");
                for (File f : apps) {

                    String propertyFile = f.getParent() + "/" + LauncherConstants.PropertiesFileName;
                    File pf = new File(propertyFile);
                    if (!pf.exists() || pf.isDirectory()) {

                        try {
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
                            //apl.setAppTitle(appTitle);
                            apl.setAppVersion(appVersion);
                            apl.setAppPath(f.getPath());
                            appList.add(apl);
                            jar.close();
                        } catch (IOException ioe) {
                            System.out.println("Can't open file: " + f.getAbsolutePath() + ", error: " + ioe.getLocalizedMessage());
                        }
                    } else {
                        Properties attr = new Properties();
                        FileInputStream fis = new FileInputStream(propertyFile);
                        attr.load(fis);
                        fis.close();
                        Application apl = new Application();
                        String appName = attr.getProperty(LauncherConstants.ApplicationName, "");
                        String appVersion = attr.getProperty(LauncherConstants.ApplicationVersion, "");
                        String appType = attr.getProperty(LauncherConstants.ApplicationType, "");
                        String appExecFile = attr.getProperty(LauncherConstants.ApplicationExecFile, "");

                        apl.setAppName(appName);
                        apl.setAppVersion(appVersion);
                        apl.setExecFile(appExecFile);
                        apl.setAppPath(f.getParentFile().getName());
                        apl.setAppType(appType);

                        appList.add(apl);
                    }
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

    public boolean installFile(String appName, String version, String fileName, InputStream is) {
        try {
            String appDir = appRoot + "/" + appName;
            fileName = appDir + "/" + fileName;
            File file = new File(fileName);
            if (!file.exists() || !file.isFile()) {
                boolean b = file.mkdirs();
                b = file.delete();
            }
            FileOutputStream fos = new FileOutputStream(file);
            int c = 0;

            byte[] array = new byte[1048576];
            while ((c = is.read(array)) != -1) {
                fos.write(array, 0, c);
            }
            fos.close();
            is.close();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
