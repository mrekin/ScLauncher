package ru.mrekin.sc.launcher.core;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * Created by MRekin on 27.08.2014.
 */
public class SettingsManager {
    static Properties settings, appProperties;

    File localSettingsFile;
    private static SettingsManager instance;

    private SettingsManager() {
        localSettingsFile = getLocalSettingsFile();
        settings = new Properties();
        appProperties = new Properties();

        if (!localSettingsFile.exists() || localSettingsFile.isDirectory() || settings.size() == 0) {
            applyDefaultSettings();
        }

        loadLauncherSettings();
        loadLauncherProperties();
        instance = this;
    }

    private static File getLocalSettingsFile() {
        return new File(LauncherConstants.WorkingDirectory + LauncherConstants.SettingsFileName);
    }

    private static File getDefaultSettingsFile() {
        try {
            return new File(SettingsManager.class.getClassLoader().getResource(LauncherConstants.SettingsFileName).toURI());
        } catch (URISyntaxException use) {
            log(use.getLocalizedMessage());
            return null;
        }
    }


    public static SettingsManager getInstance() {
        if (instance != null) {

            return instance;
        } else {
            return new SettingsManager();
        }
    }

    /**
     * Return setting value or property value or empty string. If exist setting and property with same name - setting will be returned.
     *
     * @param name
     * @return
     */
    public static String getPropertyByName(String name) {

        return getPropertyByName(name, "");
    }

    /**
     * Return setting value or property value or empty string. If exist setting and property with same name - setting will be returned.
     *
     * @param name
     * @return
     */
    public static String getPropertyByName(String name, String defValue) {
        return settings.getProperty(name, appProperties.getProperty(name, defValue));
    }

    public void loadLauncherSettings() {
        try {
            settings.load(new FileInputStream(localSettingsFile));
        } catch (IOException ioe) {
            log(ioe.getLocalizedMessage());
        }
    }

    public void loadLauncherProperties() {
        try {
            appProperties.load(SettingsManager.class.getClassLoader().getResourceAsStream(LauncherConstants.PropertiesFileName));
        } catch (IOException ioe) {
            log(ioe.getLocalizedMessage());
        }
    }

    private static void log(Object o) {
        System.out.println(o.toString());
    }

    /**
     * Copy default settings to workDir.
     */
    private static void applyDefaultSettings() {
        InputStream is = SettingsManager.class.getClassLoader().getResourceAsStream(LauncherConstants.SettingsFileName);
        try {
            FileOutputStream fos = new FileOutputStream(getLocalSettingsFile());
            int c;
            while ((c = is.read()) != -1) {
                fos.write(c);
            }
            is.close();
            fos.close();
        } catch (IOException ioe) {
            log(ioe.getLocalizedMessage());
        }

    }

    public static boolean updateLocalSettings() {

        Properties localSettings = new Properties();
        Properties tempLocalSettings = new Properties();
        Properties defaultSettings = new Properties();
        try {
            File lsf = getLocalSettingsFile();
            if (!lsf.exists() || !lsf.isFile()) {
                applyDefaultSettings();
            } else {
                localSettings.load(new FileInputStream(lsf));
                tempLocalSettings = (Properties)localSettings.clone();
                File dsf = getDefaultSettingsFile();
                if(!dsf.exists()||!dsf.isFile()){
                    log("Error on check default settings file");
                    return false;
                }
                defaultSettings.load(new FileInputStream(dsf));
                //if need to force updete
                //localSettings.putAll(defaultSettings);
                //if need to add new properties
                for(String key: defaultSettings.stringPropertyNames()){
                    if(!localSettings.containsKey(key)){
                        localSettings.put(key,defaultSettings.getProperty(key));
                    }
                }
                if(!localSettings.equals(tempLocalSettings)){
                    localSettings.store(new FileOutputStream(lsf),"test");
                }

            }
        } catch (FileNotFoundException fnfe) {
            log(fnfe.getLocalizedMessage());
            return false;
        } catch (IOException ioe) {
            log(ioe.getLocalizedMessage());
            return false;
        }
        return true;
    }

}
