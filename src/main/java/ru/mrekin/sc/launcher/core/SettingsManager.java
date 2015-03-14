package ru.mrekin.sc.launcher.core;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;

import java.io.*;
import java.util.*;

/**
 * Created by MRekin on 27.08.2014.
 */
public class SettingsManager {
    private Properties settings, appProperties, pluginProperties;
    private XMLConfiguration xmlConfiguration;
    private static SettingsManager instance;
    File localSettingsFile;

    private SettingsManager() {

        System.setProperty("java.net.preferIPv4Stack", "true");

        localSettingsFile = getLocalSettingsFile();
        settings = new Properties();

        pluginProperties = new Properties();

        appProperties = new Properties();


        if (!localSettingsFile.exists() || localSettingsFile.isDirectory()) {
            applyDefaultSettings();
        }

        loadLauncherSettings();
        loadLauncherProperties();
        instance = this;

        //TODO Need to use XMLConfiguration for config management, Properties not very usefull
        xmlConfiguration = loadXMLConfiguration(settings);


    }

    private XMLConfiguration loadXMLConfiguration(Properties defaultProperties) {
        XMLConfiguration xmlConfiguration = null;
        try {
            File file = new File(LauncherConstants.SettingsFileName2);
            if (!file.exists() || !file.isFile()) {
                //first run
                try {

                    xmlConfiguration = new XMLConfiguration();
                    for (Object key : settings.keySet()) {
                        if (((String) key).matches("[_a-zA-Z0-9 -]*")) {
                            xmlConfiguration.addProperty((String) key, settings.get(key));
                        }
                    }
                    file.createNewFile();
                    xmlConfiguration.save(file);
                } catch (IOException ioe) {
                    log(ioe.getLocalizedMessage());
                }
            } else {
                xmlConfiguration = new XMLConfiguration(file);
            }
        } catch (ConfigurationException ce) {
            log(ce.getLocalizedMessage());
            xmlConfiguration = new XMLConfiguration();
        }
        xmlConfiguration.addConfigurationListener(new ConfigurationListener() {

            public void configurationChanged(ConfigurationEvent configurationEvent) {
                //if(configurationEvent.getType()== AbstractConfiguration.){
                //}

                log("Changed property: " + configurationEvent.getPropertyName());
            }
        });

        return xmlConfiguration;
    }

    private static File getLocalSettingsFile() {
        return new File(LauncherConstants.SettingsFileName);
    }

    public XMLConfiguration getXmlConfiguration() {
        return xmlConfiguration;
    }

    private static InputStream getDefaultSettings() {

        return SettingsManager.class.getClassLoader().getResourceAsStream(LauncherConstants.SettingsFileName);
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
    public String getPropertyByName(String name) {

        return getPropertyByName(name, "");
    }

    /**
     * Return setting value or property value or empty string. If exist setting and property with same name - setting will be returned.
     *
     * @param name
     * @return
     */
    public String getPropertyByName1(String name, String defValue) {
        return settings.getProperty(name, appProperties.getProperty(name, pluginProperties.getProperty(name, defValue)));
    }

    /**
     * Return setting value or property value or empty string. If exist setting and property with same name - setting will be returned.
     *
     * @param name
     * @return
     */
    public String getPropertyByName(String name, String defValue) {
        String value = (String) xmlConfiguration.getProperty(name);
        return "".equals(value) ? appProperties.getProperty(name, pluginProperties.getProperty(name, defValue)) : value;
        //return xmlConfiguration.getProperty(name, appProperties.getProperty(name, pluginProperties.getProperty(name, defValue)));
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

        //Properties with sorted keys
        Properties localSettings = new Properties() {
            @Override
            public synchronized Enumeration<Object> keys() {
                return Collections.enumeration(new TreeSet<Object>(super.keySet()));
            }
        };
        Properties tempLocalSettings;// = new Properties();
        Properties defaultSettings = new Properties();
        try {
            File lsf = getLocalSettingsFile();
            log(lsf.getAbsolutePath());
            if (!lsf.exists() || !lsf.isFile()) {
                log("Local settings file " + lsf.getAbsolutePath() + " not found");
                applyDefaultSettings();
            } else {
                localSettings.load(new FileInputStream(lsf));
                tempLocalSettings = (Properties) localSettings.clone();
                InputStream dsf = getDefaultSettings();
                if (dsf == null) {
                    log("Can't load default settings. Jar file is corrupted or you need to package project first.");
                    System.exit(1);
                }
                defaultSettings.load(dsf);

                for (String key : defaultSettings.stringPropertyNames()) {
                    if (!localSettings.containsKey(key)) {
                        localSettings.put(key, defaultSettings.getProperty(key));
                    }
                }
                if (!localSettings.equals(tempLocalSettings)) {
                    localSettings.store(new FileOutputStream(lsf), null);
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

    public void loadPluginProperties() {
        try {
            Properties temp = new Properties();
            ArrayList<File> props = FileDriver.listFiles(SettingsManager.getInstance().getPropertyByName(LauncherConstants.PluginDirectory, "plugins"), ".props");
            for (File f : props) {
                temp.clear();
                temp.load(new FileInputStream(f));
                for (String s : temp.stringPropertyNames()) {
                    if (!pluginProperties.contains(s)) {
                        //TODO how to find plugin Name ? Now it will be without any prefixes
                        pluginProperties.put(s, temp.getProperty(s));
                    }
                }
            }
            //appProperties.load());
        } catch (IOException ioe) {
            log(ioe.getLocalizedMessage());
        }
    }

}
