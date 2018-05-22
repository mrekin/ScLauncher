package ru.mrekin.sc.launcher.core;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

/**
 * Created by MRekin on 27.08.2014.
 */
public class SettingsManager {
    private Properties appProperties, pluginProperties;
    private XMLConfiguration xmlConfiguration, settings;
    private static SettingsManager instance;
    File localSettingsFile;

    private static void log(String msg) {
        SCLogger.getInstance().log(MethodHandles.lookup().lookupClass().getName(), "INFO", msg);
    }

    private SettingsManager() {

        System.setProperty("java.net.preferIPv4Stack", "true");

        localSettingsFile = getLocalSettingsFile();
        settings = new XMLConfiguration();

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

        //  LauncherGui.getInstance().launch();
    }

    private XMLConfiguration loadXMLConfiguration(XMLConfiguration defaultProperties) {
        XMLConfiguration xmlConfiguration = null;
        try {
            File file = new File(LauncherConstants.SettingsFileName2);
            if (!file.exists() || !file.isFile()) {
                //first run
                try {
                    log("Applying old configuration.");
                    applyDefaultSettings();
                    xmlConfiguration = new XMLConfiguration(file);
                } catch (Exception ioe) {
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

                //log("Changed property: " + configurationEvent.getPropertyName());
            }
        });

        return xmlConfiguration;
    }

    /**
     * @return
     */
    private static File getLocalSettingsFile() {
        return new File(LauncherConstants.SettingsFileName2);
    }

    public void save() {
        try {
            xmlConfiguration.save(getLocalSettingsFile());
        } catch (ConfigurationException ce) {
            log(ce.getLocalizedMessage());
        }
    }

    public XMLConfiguration getXmlConfiguration() {
        return xmlConfiguration;
    }

    private static InputStream getDefaultSettings() {

        return SettingsManager.class.getClassLoader().getResourceAsStream(LauncherConstants.SettingsFileName2);
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
 /*   public String getPropertyByName1(String name, String defValue) {
        return settings.getProperty(name, appProperties.getProperty(name, pluginProperties.getProperty(name, defValue)));
    }
*/

    /**
     * Return setting value or property value or empty string. If exist setting and property with same name - setting will be returned.
     *
     * @param name
     * @return
     */
    public String getPropertyByName(String name, String defValue) {
        String[] test = xmlConfiguration.getStringArray(name);
        String value = (String) xmlConfiguration.getProperty(name);
        return value == null || "".equals(value) ? appProperties.getProperty(name, pluginProperties.getProperty(name, defValue)) : value;
        //return xmlConfiguration.getProperty(name, appProperties.getProperty(name, pluginProperties.getProperty(name, defValue)));
    }

    /**
     * Return setting value or property value or empty string. If exist setting and property with same name - setting will be returned.
     *
     * @param name
     * @return
     */
    public String[] getPropertiesArrayByName(String name, String defValue) {
        String[] value = xmlConfiguration.getStringArray(name);
        if (value == null || value.length == 0 || (value.length == 1 && "".equals(value[0]))) {
            String property = appProperties.getProperty(name, pluginProperties.getProperty(name, defValue));
            if (!"".equals(property)) {
                value = new String[]{property};
            } else {
                value = new String[0];
            }
        }
        return value;

    }

    /**
     * Return setting value or property value or empty string. If exist setting and property with same name - setting will be returned.
     *
     * @param name
     * @return
     */
    public String[] getPropertiesArrayByName(String name) {
        return getPropertiesArrayByName(name, "");
    }


    private static void log(Object o) {
        log(o.toString());
    }

    /**
     * Copy default settings to workDir.
     */
    private static void applyDefaultSettings() {

        InputStream is = SettingsManager.class.getClassLoader().getResourceAsStream(LauncherConstants.SettingsFileName2);
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

        //System.setProperty("ru.mrekin.sc.sclauncher.forceSettings", "true");
//      System.getProperty("ru.mrekin.sc.sclauncher.forceSettings", "true");
        boolean forceSettings = Boolean.getBoolean("ru.mrekin.sc.sclauncher.forceSettings");
        log("Force setting is: " + forceSettings);
        //Properties with sorted keys
        XMLConfiguration localSettings;
        XMLConfiguration tempLocalSettings;// = new Properties();
        XMLConfiguration defaultSettings = new XMLConfiguration();
        try {
            File lsf = getLocalSettingsFile();
            log(lsf.getAbsolutePath());
            if (!lsf.exists() || !lsf.isFile()) {
                log("Local settings file " + lsf.getAbsolutePath() + " not found");
                applyDefaultSettings();
            } else {
                localSettings = new XMLConfiguration(lsf);
                tempLocalSettings = (XMLConfiguration) localSettings.clone();
                InputStream dsf = getDefaultSettings();
                if (dsf == null) {
                    log("Can't load default settings. Jar file is corrupted or you need to package project first.");
                    System.exit(1);
                }
                defaultSettings.load(dsf);

                Iterator iterator = defaultSettings.getKeys();
                String key;
                while (iterator.hasNext())
                //for (String key : defaultSettings.stringPropertyNames())
                {
                    key = (String) iterator.next();
                    if (!localSettings.containsKey(key) && !"".equals(key)) {
                        localSettings.addProperty(key, defaultSettings.getProperty(key));
                    } else {
                        if (forceSettings && "true".equals(defaultSettings.getString(key + "[@force]"))) {
                            localSettings.setProperty(key, defaultSettings.getProperty(key));
                            log("Force update property: " + key);
                        }
                    }
                }
                if (!localSettings.equals(tempLocalSettings)) {
                    localSettings.save(lsf);
                }

            }
        } catch (ConfigurationException ce) {
            log(ce.getLocalizedMessage());
            return false;
        }

        return true;
    }

    public void loadLauncherSettings() {
        try {
            settings = new XMLConfiguration(localSettingsFile);
        } catch (ConfigurationException ce) {
            log(ce.getLocalizedMessage());
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
